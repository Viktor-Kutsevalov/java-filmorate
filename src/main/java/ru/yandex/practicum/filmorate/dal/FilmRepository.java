package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;   // <-- НОВЫЙ ИМПОРТ
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    private static final String FIND_ALL = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            """;
    private static final String FIND_BY_ID = FIND_ALL + " WHERE f.id = ?";
    private static final String INSERT = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM films WHERE id = ?";
    private static final String FIND_POPULAR = """
            SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) as like_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY like_count DESC
            LIMIT ?
            """;
    private static final String FIND_COMMON = """
            SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) as like_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id FROM film_likes WHERE user_id = ?
                INTERSECT
                SELECT film_id FROM film_likes WHERE user_id = ?
            )
            GROUP BY f.id
            ORDER BY like_count DESC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbcTemplate = jdbc;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
    }

    public List<Film> findAll() {
        List<Film> films = findMany(FIND_ALL);
        loadGenresAndLikes(films);
        loadDirectors(films);
        return films;
    }

    public Optional<Film> findById(long id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID, id);
        filmOpt.ifPresent(film -> {
            loadGenresAndLikes(List.of(film));
            loadDirectors(List.of(film));
        });
        return filmOpt;
    }

    public Film save(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null
        );
        film.setId(id);
        updateGenres(film);
        updateDirectors(film);
        return film;
    }

    public Film update(Film film) {
        update(UPDATE,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        updateGenres(film);
        updateDirectors(film);
        return film;
    }

    public boolean deleteById(long id) {
        return delete(DELETE, id);
    }

    public List<Film> findPopular(int limit) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR, mapper, limit);
        loadGenresAndLikes(films);
        loadDirectors(films);
        return films;
    }

    public List<Film> findFilmsByDirector(int directorId, String sortBy) {
        String orderBy = "year".equalsIgnoreCase(sortBy)
                ? "f.release_date"
                : "COUNT(fl.user_id) DESC";

        String sql = """
                SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) as like_count
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                JOIN film_director fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id
                ORDER BY """ + orderBy;

        List<Film> films = jdbcTemplate.query(sql, mapper, directorId);
        loadGenresAndLikes(films);
        loadDirectors(films);
        return films;
    }

    public List<Film> findCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbcTemplate.query(FIND_COMMON, mapper, userId, friendId);
        loadGenresAndLikes(films);
        return films;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(g -> new Object[]{film.getId(), g.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void loadGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Set<Genre>> genresMap = loadGenresForFilms(films);
        Map<Long, Set<Long>> likesMap = loadLikesForFilms(films);
        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.getLikes().clear();
            film.getLikes().addAll(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private Map<Long, Set<Genre>> loadGenresForFilms(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);
        String query = "SELECT fg.film_id, g.id, g.name FROM film_genre fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id IN (:ids)";
        Map<Long, Set<Genre>> result = new HashMap<>();
        namedJdbcTemplate.query(query, params, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        });
        return result;
    }

    private Map<Long, Set<Long>> loadLikesForFilms(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);
        String query = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (:ids)";
        Map<Long, Set<Long>> result = new HashMap<>();
        namedJdbcTemplate.query(query, params, rs -> {
            long filmId = rs.getLong("film_id");
            long userId = rs.getLong("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });
        return result;
    }

    private void updateDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
            List<Object[]> batchArgs = film.getDirectors().stream()
                    .map(d -> new Object[]{film.getId(), d.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void loadDirectors(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Set<Director>> directorsMap = loadDirectorsForFilms(films);
        for (Film film : films) {
            film.setDirectors(directorsMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private Map<Long, Set<Director>> loadDirectorsForFilms(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);
        String query = """
                SELECT fd.film_id, d.id, d.name
                FROM film_director fd
                JOIN directors d ON fd.director_id = d.id
                WHERE fd.film_id IN (:ids)
                """;
        Map<Long, Set<Director>> result = new HashMap<>();
        namedJdbcTemplate.query(query, params, rs -> {
            long filmId = rs.getLong("film_id");
            Director director = new Director();
            director.setId(rs.getInt("id"));
            director.setName(rs.getString("name"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        });
        return result;
    }
}