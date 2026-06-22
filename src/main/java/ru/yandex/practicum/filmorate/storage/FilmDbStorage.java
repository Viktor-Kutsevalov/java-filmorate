package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.LikeRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;

    @Override
    public Film add(Film film) {
        return filmRepository.save(film);
    }

    @Override
    public Film update(Film film) {
        return filmRepository.update(film);
    }

    @Override
    public Optional<Film> getById(Long id) {
        return filmRepository.findById(id);
    }

    @Override
    public List<Film> getAll() {
        return filmRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        filmRepository.deleteById(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likeRepository.addLike(filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        likeRepository.removeLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return filmRepository.findPopular(count);
    }
}