package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;
    private final FilmRepository filmRepository;

    public List<Film> findRecommendations(Long userId, int limit) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mr.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id
                JOIN film_likes fl2 ON f.id = fl2.film_id
                WHERE fl2.user_id IN (
                    SELECT fl4.user_id
                    FROM film_likes fl3
                    JOIN film_likes fl4 ON fl3.film_id = fl4.film_id
                    WHERE fl3.user_id = ? AND fl4.user_id != ?
                    GROUP BY fl4.user_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 5
                )
                AND f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mr.name
                ORDER BY f.release_date DESC
                """;

        List<Film> films = jdbc.query(sql, filmMapper, userId, userId, userId);
        filmRepository.loadGenresAndDirectors(films);
        return films;
    }
}