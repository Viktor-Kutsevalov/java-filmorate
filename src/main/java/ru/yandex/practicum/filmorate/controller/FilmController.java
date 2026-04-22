package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MIN_DURATION = 1;

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Добавление фильма: {}", film);
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.debug("Фильм добавлен с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        validateFilm(film);
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм: {}", film);
            throw new ValidationException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.debug("Фильм с id={} обновлён", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка валидации: описание длиной {} символов (макс. {})",
                    film.getDescription().length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Описание не должно превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации: дата релиза {} (мин. {})", film.getReleaseDate(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() == null || film.getDuration() < MIN_DURATION) {
            log.warn("Ошибка валидации: продолжительность {} (мин. {})", film.getDuration(), MIN_DURATION);
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }
}