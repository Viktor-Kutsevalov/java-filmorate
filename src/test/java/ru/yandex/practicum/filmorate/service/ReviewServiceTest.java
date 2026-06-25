package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
public class ReviewServiceTest {

    @Autowired
    private ReviewService rS;

    private Review makeReview(Long userId, Long filmId, String content, Boolean isPositive) {
        Review r = new Review();
        r.setUserId(userId);
        r.setFilmId(filmId);
        r.setContent(content);
        r.setIsPositive(isPositive);
        return r;
    }

    private Review save(Long userId, Long filmId, String content, Boolean isPositive) {
        return rS.addReview(makeReview(userId, filmId, content, isPositive));
    }

    @Test
    @DisplayName("Создание отзыва")
    void addReview() {
        Review r = makeReview(1L, 1L, "Отзыв", true);

        Review saved = rS.addReview(r);

        assertThat(saved.getReviewId()).isNotNull();
        assertThat(saved.getUseful()).isZero();
        assertThat(saved.getContent()).isEqualTo("Отзыв");
        assertThat(saved.getIsPositive()).isTrue();
    }

    @Test
    @DisplayName("Создать отзыв с пустым контентом")
    void addReviewEmptyContent() {
        Review r = makeReview(1L, 1L, "", true);

        assertThrows(ValidationException.class, () -> rS.addReview(r));
    }

    @Test
    @DisplayName("Создать отзыв с пустой оценкой")
    void addReviewNullIsPositive() {
        Review r = makeReview(1L, 1L, "Отзыв", null);

        assertThrows(ValidationException.class, () -> rS.addReview(r));
    }

    @Test
    @DisplayName("Создать отзыв с несуществующим пользователем")
    void addReviewNotFoundUser() {
        Review r = makeReview(999L, 1L, "Отзыв", true);

        assertThrows(NotFoundException.class, () -> rS.addReview(r));
    }

    @Test
    @DisplayName("Создать отзыв с несуществующим фильмом")
    void addReviewNotFoundFilm() {
        Review r = makeReview(1L, 999L, "Отзыв", true);

        assertThrows(NotFoundException.class, () -> rS.addReview(r));
    }

    @Test
    @DisplayName("Получить отзыв по id")
    void getReviewById() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        Review found = rS.getReviewById(id);

        assertThat(found.getReviewId()).isEqualTo(id);
        assertThat(found.getContent()).isEqualTo("Отзыв");
    }

    @Test
    @DisplayName("Получить отзыв по несуществующему id")
    void getReviewByIdNotFound() {
        assertThrows(NotFoundException.class, () -> rS.getReviewById(999L));
    }

    @Test
    @DisplayName("Получить отзывы по filmId")
    void getReviewsByFilmId() {
        save(1L, 1L, "Отзыв1", true);
        save(2L, 1L, "Отзыв2", true);

        List<Review> result = rS.getReviews(1L, 10);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getFilmId() == 1L);
    }

    @Test
    @DisplayName("Получить отзывы по filmId без отзывов")
    void getReviewsBadFilmEmpty() {
        save(1L, 1L, "Отзыв", true);

        assertThat(rS.getReviews(999L, 10)).isEmpty();
    }

    @Test
    @DisplayName("Получить все отзывы, сортировка по убыванию полезности")
    void getReviewsSorted() {
        Long id1 = save(1L, 1L, "Отзыв1", true).getReviewId();
        Long id2 = save(2L, 1L, "Отзыв2", true).getReviewId();
        rS.addLikeReview(id2, 2L);

        List<Review> result = rS.getReviews(null, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewId()).isEqualTo(id2);
    }

    @Test
    @DisplayName("Получить отзывы с count=1")
    void getReviewsCountLimit() {
        save(1L, 1L, "Отзыв1", true);
        save(2L, 1L, "Отзыв2", true);

        assertThat(rS.getReviews(null, 1)).hasSize(1);
    }

    @Test
    @DisplayName("Получить отзывы с count=0")
    void getReviewsCountZero() {
        assertThrows(ValidationException.class, () -> rS.getReviews(null, 0));
    }

    @Test
    @DisplayName("Получить отзывы с count отрицательным")
    void getReviewsCountNegative() {
        assertThrows(ValidationException.class, () -> rS.getReviews(null, -1));
    }

    @Test
    @DisplayName("Обновить отзыв")
    void updateReview() {
        Review saved = save(1L, 1L, "Старый", true);
        saved.setContent("Новый");

        rS.updateReview(saved);
        Review updated = rS.getReviewById(saved.getReviewId());

        assertThat(updated.getContent()).isEqualTo("Новый");
    }

    @Test
    @DisplayName("Обновить несуществующий отзыв")
    void updateReviewNotFound() {
        Review r = makeReview(1L, 1L, "Отзыв", true);
        r.setReviewId(999L);

        assertThrows(NotFoundException.class, () -> rS.updateReview(r));
    }

    @Test
    @DisplayName("Обновить отзыв с пустым контентом")
    void updateReviewEmptyContent() {
        Review saved = save(1L, 1L, "Отзыв", true);
        saved.setContent("");

        assertThrows(ValidationException.class, () -> rS.updateReview(saved));
    }

    @Test
    @DisplayName("Удалить отзыв")
    void removeReview() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.removeReview(id);

        assertThrows(NotFoundException.class, () -> rS.getReviewById(id));
    }

    @Test
    @DisplayName("Удалить несуществующий отзыв")
    void removeReviewNotFound() {
        assertThrows(NotFoundException.class, () -> rS.removeReview(999L));
    }

    @Test
    @DisplayName("Поставить лайк")
    void addLikeIncreasesUseful() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.addLikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(1);
    }

    @Test
    @DisplayName("Повторный лайк, ничего не меняет")
    void addLikeTwice() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.addLikeReview(id, 2L);
        rS.addLikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(1);
    }

    @Test
    @DisplayName("Лайк после дизлайка")
    void addLikeAfterDislike() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();
        rS.addDislikeReview(id, 2L);

        rS.addLikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(1);
    }

    @Test
    @DisplayName("Поставить дизлайк")
    void addDislike() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.addDislikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Повторный дизлайк")
    void addDislikeTwice() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.addDislikeReview(id, 2L);
        rS.addDislikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Дизлайк после лайка")
    void addDislikeAfterLike() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();
        rS.addLikeReview(id, 2L);

        rS.addDislikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Снять лайк")
    void removeLike() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();
        rS.addLikeReview(id, 2L);

        rS.removeLikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isZero();
    }

    @Test
    @DisplayName("Снять дизлайк")
    void removeDislike() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();
        rS.addDislikeReview(id, 2L);

        rS.removeDislikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isZero();
    }

    @Test
    @DisplayName("Снять лайк без лайка")
    void removeLikeWithoutExisting() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.removeLikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isZero();
    }

    @Test
    @DisplayName("Снять дизлайк без дизлайка")
    void removeDislikeWithoutExisting() {
        Long id = save(1L, 1L, "Отзыв", true).getReviewId();

        rS.removeDislikeReview(id, 2L);

        assertThat(rS.getReviewById(id).getUseful()).isZero();
    }
}