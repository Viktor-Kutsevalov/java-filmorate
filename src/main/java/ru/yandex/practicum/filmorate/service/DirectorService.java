package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public List<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(int id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        validateDirector(director);
        return directorRepository.save(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director);
        if (director.getId() == 0 || directorRepository.findById(director.getId()).isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + director.getId() + " не найден");
        }
        return directorRepository.update(director);
    }

    public void deleteDirector(int id) {
        getDirectorById(id);
        directorRepository.deleteById(id);
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
    }
}