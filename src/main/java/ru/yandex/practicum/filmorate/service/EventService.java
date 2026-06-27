package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    public List<Event> getFeed(Long userId) {
        return eventRepository.getFeedByUserId(userId);
    }
}