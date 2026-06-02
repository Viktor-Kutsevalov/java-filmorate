package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreRepository extends BaseRepository<Genre> {

    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_ALL_BY_IDS = "SELECT * FROM genres WHERE id IN (%s)";

    public GenreRepository(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<Genre> findAllByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format(FIND_ALL_BY_IDS, placeholders);
        return jdbc.query(sql, mapper, ids.toArray());
    }
}