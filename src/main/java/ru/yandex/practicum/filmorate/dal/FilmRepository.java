package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
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
        return films;
    }

    public Optional<Film> findById(long id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID, id);
        filmOpt.ifPresent(film -> loadGenresAndLikes(List.of(film)));
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
        return film;
    }

    public boolean deleteById(long id) {
        return delete(DELETE, id);
    }

    public List<Film> findPopular(int limit) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR, mapper, limit);
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
}