package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review add(Review review);

    Review update(Review review);

    Optional<Review> getByReviewId(Long id);

    List<Review> getByFilmId(Long filmId, int limit);

    void deleteById(Long id);

    void updateUseful(long reviewId, int delta);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);

    Optional<String> getExistingType(Long reviewId, Long userId);
}
