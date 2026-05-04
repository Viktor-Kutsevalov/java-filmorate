package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        film.getLikes().add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        if (!film.getLikes().remove(userId)) {
            log.warn("Попытка удалить несуществующий лайк от {} у фильма {}", userId, filmId);
        } else {
            log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        }
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = DEFAULT_POPULAR_COUNT;
        }
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }
}
