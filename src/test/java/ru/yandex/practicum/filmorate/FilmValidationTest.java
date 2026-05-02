package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {
    private final FilmController controller = new FilmController();

    @Test
    void shouldNotAddFilmWithBlankName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldNotAddFilmWithLongDescription() {
        Film film = new Film();
        film.setName("Русский фильм");
        film.setDescription("а".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldNotAddFilmWithReleaseDateBefore1895_12_28() {
        Film film = new Film();
        film.setName("Старый фильм");
        film.setDescription("Интересное кино");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldNotAddFilmWithNonPositiveDuration() {
        Film film = new Film();
        film.setName("Короткометражка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-5);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldAddValidFilm() {
        Film film = new Film();
        film.setName("Валидный фильм");
        film.setDescription("Корректное описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        assertDoesNotThrow(() -> controller.addFilm(film));
    }
}