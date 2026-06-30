package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewLikeRepository {
    private final JdbcTemplate jdbc;

    public void addLike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_likes (review_id, user_id, type) VALUES (?, ?, ?)";
        jdbc.update(sql, reviewId, userId, "LIKE");
    }

    public void addDislike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_likes (review_id, user_id, type) VALUES (?, ?, ?)";
        jdbc.update(sql, reviewId, userId, "DISLIKE");
    }

    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND type = ?";
        jdbc.update(sql, reviewId, userId, "LIKE");
    }

    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND type = ?";
        jdbc.update(sql, reviewId, userId, "DISLIKE");
    }

    public Optional<String> getExistingType(Long reviewId, Long userId) {
        String sql = "SELECT type FROM review_likes WHERE review_id = ? AND user_id = ?";
        try {
            String type = jdbc.queryForObject(sql, String.class, reviewId, userId);
            return Optional.ofNullable(type);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
