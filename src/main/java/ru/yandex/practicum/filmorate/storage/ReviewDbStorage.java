package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.ReviewLikeRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Override
    public Review add(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public Review update(Review review) {
        return reviewRepository.update(review);
    }

    @Override
    public Optional<Review> getByReviewId(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public List<Review> getByFilmId(Long filmId, int limit) {
        return reviewRepository.findByFilmId(filmId, limit);
    }

    @Override
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public void updateUseful(long reviewId, int delta) {
        reviewRepository.updateUseful(reviewId, delta);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        reviewLikeRepository.addLike(reviewId, userId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        reviewLikeRepository.addDislike(reviewId, userId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        reviewLikeRepository.removeLike(reviewId, userId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        reviewLikeRepository.removeDislike(reviewId, userId);
    }

    @Override
    public Optional<String> getExistingType(Long reviewId, Long userId) {
        return reviewLikeRepository.getExistingType(reviewId, userId);
    }
}
