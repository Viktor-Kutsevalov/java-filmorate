package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final FilmRepository filmRepository;
    private final DirectorRepository directorRepository;
    private final EventService eventService;

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        validateMpaAndGenresAndDirectors(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        if (film.getId() == null || filmStorage.getById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        validateMpaAndGenresAndDirectors(film);
        return filmStorage.update(film);
    }

    public Film addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        validateUserById(userId);
        filmStorage.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return getFilmById(filmId);
    }

    public Film removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        validateUserById(userId);
        filmStorage.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
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

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorRepository.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + directorId + " не найден"));

        if ((!"year".equalsIgnoreCase(sortBy) && !"likes".equalsIgnoreCase(sortBy))) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }
        return filmRepository.findFilmsByDirector(directorId, sortBy);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        validateUserById(userId);
        validateUserById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void deleteFilm(Long filmId) {
        getFilmById(filmId);
        filmStorage.deleteById(filmId);
        log.debug("Фильм с id={} удалён", filmId);
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Параметр query не может быть пустым");
        }
        if (by == null || by.isBlank()) {
            throw new ValidationException("Параметр by не может быть пустым");
        }
        List<String> searchBy = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Set<String> validValues = Set.of("title", "director");
        for (String s : searchBy) {
            if (!validValues.contains(s)) {
                throw new ValidationException("Параметр by может содержать только 'title' и/или 'director'");
            }
        }
        return filmRepository.searchFilms(query, searchBy);
    }

    private void validateUserById(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
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

    private void validateMpaAndGenresAndDirectors(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != 0) {
            mpaRepository.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            List<Genre> existingGenres = genreRepository.findAllByIds(genreIds);
            if (existingGenres.size() != genreIds.size()) {
                Set<Integer> existingIds = existingGenres.stream().map(Genre::getId).collect(Collectors.toSet());
                Set<Integer> missingIds = new HashSet<>(genreIds);
                missingIds.removeAll(existingIds);
                throw new NotFoundException("Жанры с id = " + missingIds + " не найдены");
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            List<Director> existingDirectors = directorRepository.findAllByIds(directorIds);
            if (existingDirectors.size() != directorIds.size()) {
                Set<Long> existingIds = existingDirectors.stream().map(Director::getId).collect(Collectors.toSet());
                Set<Long> missingIds = new HashSet<>(directorIds);
                missingIds.removeAll(existingIds);
                throw new NotFoundException("Режиссёры с id = " + missingIds + " не найдены");
            }
        }
    }
}