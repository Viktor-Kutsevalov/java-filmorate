package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MIN_DURATION = 1;
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        validateMpaAndGenres(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        if (film.getId() == null || filmStorage.getById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public Film addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.addLike(filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return getFilmById(filmId);
    }

    public Film removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        return getFilmById(filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count != null && count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        int limit = (count == null) ? DEFAULT_POPULAR_COUNT : count;
        return filmStorage.getPopularFilms(limit);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание не должно превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() == null || film.getDuration() < MIN_DURATION) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != 0) {
            mpaRepository.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
        }
        // Проверка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreRepository.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + genre.getId() + " не найден"));
            }
        }
    }
}