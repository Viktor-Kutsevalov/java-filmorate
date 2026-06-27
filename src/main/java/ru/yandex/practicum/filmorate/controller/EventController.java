package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{id}/feed")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public List<Event> getFeed(@PathVariable Long id) {
        return eventService.getFeed(id);
    }
}