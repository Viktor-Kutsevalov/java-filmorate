package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepository {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbc.update(sql, userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sql, userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friends f ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;
        return jdbc.query(sql, userRowMapper, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friends f1 ON u.id = f1.friend_id
                JOIN friends f2 ON u.id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;
        return jdbc.query(sql, userRowMapper, userId, otherId);
    }
}