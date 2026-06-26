package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
public class FilmServiceTest {

    @Autowired
    private FilmService fS;

    @Test
    @DisplayName("Общие фильмы")
    void getCommonFilms() {
        fS.addLike(1L, 1L);
        fS.addLike(1L, 2L);

        List<Film> result = fS.getCommonFilms(1L, 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Общие фильмы пустые")
    void getCommonFilmsEmpty() {
        fS.addLike(1L, 1L);

        assertThat(fS.getCommonFilms(1L, 2L)).isEmpty();
    }

    @Test
    @DisplayName("Общие фильмы несуществующий userId")
    void getCommonFilmsNotFoundUser() {
        assertThrows(NotFoundException.class, () -> fS.getCommonFilms(999L, 1L));
    }

    @Test
    @DisplayName("Общие фильмы несуществующий friendId")
    void getCommonFilmsNotFoundFriend() {
        assertThrows(NotFoundException.class, () -> fS.getCommonFilms(1L, 999L));
    }

    @Test
    @DisplayName("Общие фильмы сортировка по популярности")
    void getCommonFilmsSortedByPopularity() {
        fS.addLike(1L, 1L);
        fS.addLike(1L, 2L);
        fS.addLike(1L, 3L);
        fS.addLike(2L, 1L);
        fS.addLike(2L, 2L);

        List<Film> result = fS.getCommonFilms(1L, 2L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Получить фильмы режиссёра, сортировка по году")
    void getFilmsByDirectorSortedByYear() {
        List<Film> films = fS.getFilmsByDirector(1L, "year");   // 1L
        assertThat(films).isNotEmpty();
        assertThat(films).extracting(Film::getReleaseDate)
                .isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    @DisplayName("Получить фильмы режиссёра, сортировка по лайкам")
    void getFilmsByDirectorSortedByLikes() {
        fS.addLike(1L, 1L);
        fS.addLike(1L, 2L);
        List<Film> films = fS.getFilmsByDirector(1L, "likes");   // 1L
        assertThat(films).isNotEmpty();
        assertThat(films.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Режиссёр не найден")
    void getFilmsByDirectorNotFound() {
        assertThrows(NotFoundException.class, () -> fS.getFilmsByDirector(999L, "year"));   // 999L
    }

    @Test
    @DisplayName("Некорректный параметр сортировки")
    void getFilmsByDirectorInvalidSort() {
        assertThrows(ValidationException.class, () -> fS.getFilmsByDirector(1L, "invalid"));   // 1L
    }
}