package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.trace("Получен запрос на получения отзыва по id={}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false, defaultValue = "10") int count
    ) {
        log.trace("Получен запрос на получение коллекции отзывов");
        return reviewService.getReviews(filmId, count);
    }

    @PostMapping
    public Review addReview(@RequestBody Review review) {
        log.trace("Получен запрос на добавления отзыва");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        log.trace("Получен запрос на обновления отзыва");
        return reviewService.updateReview(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.trace("Получен запрос пользователя {} поставить лайк отзыву {}", userId, id);
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.trace("Получен запрос пользователя {} поставить дизлайк отзыву {}", userId, id);
        reviewService.addDislikeReview(id, userId);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable Long id) {
        log.trace("Получен запрос на удаление отзыва c id={}", id);
        reviewService.removeReview(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.trace("Получен запрос на удаление оценки пользователя {} отзыва c id={}", userId, id);
        reviewService.removeLikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.trace("Получен запрос на удаление дизлайка пользователя {} отзыва c id={}", userId, id);
        reviewService.removeDislikeReview(id, userId);
    }

}
