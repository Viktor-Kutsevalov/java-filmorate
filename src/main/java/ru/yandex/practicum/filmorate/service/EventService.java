package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserStorage userStorage;

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    public List<Event> getFeed(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return eventRepository.getFeedByUserId(userId);
    }
}