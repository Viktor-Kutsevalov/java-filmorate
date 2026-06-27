package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;


import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        String sql = """
            INSERT INTO events (user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?)
            """;

        jdbc.update(sql, userId, eventType.name(), operation.name(), entityId);
    }

    public List<Event> getFeedByUserId(Long userId) {
        String sql = """
                SELECT * FROM events
                WHERE user_id = ?
                ORDER BY timestamp
                """;

        return jdbc.query(sql, eventRowMapper, userId);
    }
}