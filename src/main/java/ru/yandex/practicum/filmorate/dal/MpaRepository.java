package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<MpaRating> {

    private static final String FIND_ALL = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    public MpaRepository(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<MpaRating> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }
}