package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;

    private static final String SELECT_BASE = """
            SELECT 
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                mr.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings mr ON f.mpa_id = mr.id
            """;

    private static final String GROUP_BY = """
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mr.name
            """;

    public List<Film> findRecommendations(Long userId, int limit) {
        System.out.println("Recommendations for user " + userId);

        List<RecommendationItem> items = new ArrayList<>();

        items.addAll(getFriendsRecommendations(userId));
        items.addAll(getGenreRecommendations(userId));
        items.addAll(getSimilarRecommendations(userId));

        Map<Long, RecommendationItem> best = new LinkedHashMap<>();
        for (RecommendationItem item : items) {
            best.merge(item.film.getId(), item, (oldItem, newItem) ->
                    oldItem.score >= newItem.score ? oldItem : newItem);
        }

        return best.values().stream()
                .sorted(Comparator.comparingDouble(RecommendationItem::getScore).reversed())
                .limit(limit)
                .map(RecommendationItem::getFilm)
                .toList();
    }


    private record RecommendationItem(Film film, double score) {
        public double getScore() {
            return score;
        }
        public Film getFilm() {
            return film;
        }
    }

    private List<RecommendationItem> getFriendsRecommendations(Long userId) {
        String sql = SELECT_BASE + """
                WHERE f.id IN (
                    SELECT fl.film_id 
                    FROM film_likes fl 
                    WHERE fl.user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)
                )
                AND f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
                """ + GROUP_BY + " ORDER BY f.release_date DESC";

        return jdbc.query(sql, filmMapper, userId, userId)
                .stream()
                .map(film -> new RecommendationItem(film, 3.0))
                .toList();
    }

    // 2. Жанры — вес 2.0
    private List<RecommendationItem> getGenreRecommendations(Long userId) {
        String sql = SELECT_BASE + """
                JOIN film_genre fg ON f.id = fg.film_id
                WHERE fg.genre_id IN (
                    SELECT fg2.genre_id
                    FROM film_likes fl
                    JOIN film_genre fg2 ON fl.film_id = fg2.film_id
                    WHERE fl.user_id = ?
                    GROUP BY fg2.genre_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 3
                )
                AND f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
                """ + GROUP_BY + " ORDER BY f.release_date DESC";

        return jdbc.query(sql, filmMapper, userId, userId)
                .stream()
                .map(film -> new RecommendationItem(film, 2.0))
                .toList();
    }

    // 3. Похожие пользователи — вес 2.5
    private List<RecommendationItem> getSimilarRecommendations(Long userId) {
        String sql = SELECT_BASE + """
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
                """ + GROUP_BY + " ORDER BY f.release_date DESC";

        return jdbc.query(sql, filmMapper, userId, userId, userId)
                .stream()
                .map(film -> new RecommendationItem(film, 2.5))
                .toList();
    }
}