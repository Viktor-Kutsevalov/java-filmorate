package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private static final String FIND_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_REVIEWS_BY_ID_FILM = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String FIND_REVIEWS_ALL = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String UPDATE = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String INSERT = "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM reviews WHERE review_id = ?";
    private static final String UPDATE_USEFUL = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    public ReviewRepository(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Optional<Review> findById(Long reviewId) {
        return findOne(FIND_REVIEW_BY_ID, reviewId);
    }

    public List<Review> findByFilmId(Long filmId, int limit) {
        if (filmId != null) {
            return findMany(FIND_REVIEWS_BY_ID_FILM, filmId, limit);
        } else {
            return findMany(FIND_REVIEWS_ALL, limit);
        }
    }

    public Review update(Review review) {
        update(UPDATE,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return findById(review.getReviewId())
                .orElseThrow(() -> new InternalServerException("Не удалось обновить отзыв"));
    }

    public Review save(Review review) {
        Long reviewId = insert(INSERT,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setReviewId(reviewId);
        return review;
    }

    public boolean deleteById(Long reviewId) {
        return delete(DELETE, reviewId);
    }

    public void updateUseful(long reviewId, int delta) {
        update(UPDATE_USEFUL, delta, reviewId);
    }
}
