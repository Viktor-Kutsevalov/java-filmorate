package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
public class DirectorServiceTest {

    @Autowired
    private DirectorService directorService;

    @Test
    @DisplayName("Получить всех режиссёров")
    void getAllDirectors() {
        List<Director> directors = directorService.getAllDirectors();
        assertThat(directors).isNotEmpty();
        assertThat(directors).extracting(Director::getName)
                .contains("Test Director 1", "Test Director 2");
    }

    @Test
    @DisplayName("Получить режиссёра по id")
    void getDirectorById() {
        Director director = directorService.getDirectorById(1L);
        assertThat(director.getId()).isEqualTo(1L);
        assertThat(director.getName()).isEqualTo("Test Director 1");
    }

    @Test
    @DisplayName("Получить режиссёра по несуществующему id")
    void getDirectorByIdNotFound() {
        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(999L));   // 999L
    }

    @Test
    @DisplayName("Создать режиссёра")
    void addDirector() {
        Director newDirector = new Director();
        newDirector.setName("New Director");
        Director saved = directorService.addDirector(newDirector);
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("New Director");

        Director found = directorService.getDirectorById(saved.getId());
        assertThat(found.getName()).isEqualTo("New Director");
    }

    @Test
    @DisplayName("Создать режиссёра с пустым именем")
    void addDirectorEmptyName() {
        Director director = new Director();
        director.setName("");
        assertThrows(ValidationException.class, () -> directorService.addDirector(director));
    }

    @Test
    @DisplayName("Обновить режиссёра")
    void updateDirector() {
        Director update = new Director();
        update.setId(1L);
        update.setName("Updated Director");
        Director updated = directorService.updateDirector(update);
        assertThat(updated.getName()).isEqualTo("Updated Director");

        Director found = directorService.getDirectorById(1L);
        assertThat(found.getName()).isEqualTo("Updated Director");
    }

    @Test
    @DisplayName("Обновить несуществующего режиссёра")
    void updateDirectorNotFound() {
        Director update = new Director();
        update.setId(999L);
        update.setName("Unknown");
        assertThrows(NotFoundException.class, () -> directorService.updateDirector(update));
    }

    @Test
    @DisplayName("Обновить режиссёра с пустым именем")
    void updateDirectorEmptyName() {
        Director update = new Director();
        update.setId(1L);
        update.setName("");
        assertThrows(ValidationException.class, () -> directorService.updateDirector(update));
    }

    @Test
    @DisplayName("Удалить режиссёра")
    void deleteDirector() {
        Director newDirector = new Director();
        newDirector.setName("ToDelete");
        Director saved = directorService.addDirector(newDirector);
        Long id = saved.getId();

        directorService.deleteDirector(id);
        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(id));
    }

    @Test
    @DisplayName("Удалить несуществующего режиссёра")
    void deleteDirectorNotFound() {
        assertThrows(NotFoundException.class, () -> directorService.deleteDirector(999L));
    }
}