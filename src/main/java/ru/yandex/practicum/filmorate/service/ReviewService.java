package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        validateReview(review);
        if (userStorage.getById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }
        if (filmStorage.getById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }
        review.setUseful(0);
        Review saved = reviewStorage.add(review);
        log.debug("Добавлен отзыв id={} пользователем {} к фильму {}", review.getReviewId(), review.getUserId(), review.getFilmId());
        return saved;
    }

    public Review updateReview(Review review) {
        validateReview(review);
        getReviewById(review.getReviewId());
        Review update = reviewStorage.update(review);
        log.debug("Обновлён отзыв id={}", update.getReviewId());
        return update;
    }

    public void removeReview(Long reviewId) {
        getReviewById(reviewId);
        reviewStorage.deleteById(reviewId);
        log.debug("Удалён отзыв id={}", reviewId);
    }

    public Review getReviewById(Long reviewId) {
        return reviewStorage.getByReviewId(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        return reviewStorage.getByFilmId(filmId, count);
    }

    public void addLikeReview(Long reviewId, Long userId) {
        addReaction(reviewId, userId, "LIKE", 1, 2);
        log.debug("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislikeReview(Long reviewId, Long userId) {
        addReaction(reviewId, userId, "DISLIKE", -1, -2);
        log.debug("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLikeReview(Long reviewId, Long userId) {
        removeReaction(reviewId, userId, "LIKE", -1);
        log.debug("Пользователь {} удалил лайк с отзыва {}", userId, reviewId);
    }

    public void removeDislikeReview(Long reviewId, Long userId) {
        removeReaction(reviewId, userId, "DISLIKE", 1);
        log.debug("Пользователь {} удалил дизлайк с отзыва {}", userId, reviewId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Отзыв не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Отзыв не может быть без оценки");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Отзыв должен быть на фильм");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("У отзыва должен быть автор");
        }
    }

    private void validateUserAndReview(Long reviewId, Long userId) {
        getReviewById(reviewId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void saveReaction(Long reviewId, Long userId, String type) {
        if ("LIKE".equals(type)) {
            reviewStorage.addLike(reviewId, userId);
        } else {
            reviewStorage.addDislike(reviewId, userId);
        }
    }

    private void deleteReaction(Long reviewId, Long userId, String type) {
        if ("LIKE".equals(type)) {
            reviewStorage.removeLike(reviewId, userId);
        } else {
            reviewStorage.removeDislike(reviewId, userId);
        }
    }

    private void addReaction(Long reviewId, Long userId, String type, int newDelta, int switchDelta) {
        validateUserAndReview(reviewId, userId);
        Optional<String> existing = reviewStorage.getExistingType(reviewId, userId);

        if (existing.isEmpty()) {
            saveReaction(reviewId, userId, type);
            reviewStorage.updateUseful(reviewId, newDelta);
        } else if (!type.equals(existing.get())) {
            saveReaction(reviewId, userId, type);
            reviewStorage.updateUseful(reviewId, switchDelta);
        }
    }

    private void removeReaction(Long reviewId, Long userId, String type, int delta) {
        validateUserAndReview(reviewId, userId);
        Optional<String> existing = reviewStorage.getExistingType(reviewId, userId);

        if (existing.isPresent() && type.equals(existing.get())) {
            deleteReaction(reviewId, userId, type);
            reviewStorage.updateUseful(reviewId, delta);
        }
    }
}
