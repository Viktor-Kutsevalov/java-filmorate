package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorRepository extends BaseRepository<Director> {

    private static final String FIND_ALL = "SELECT * FROM directors ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM directors WHERE id = ?";
    private static final String FIND_ALL_BY_IDS = "SELECT * FROM directors WHERE id IN (%s)";

    public DirectorRepository(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Director> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    public Director save(Director director) {
        long id = insert(INSERT, director.getName());
        director.setId((int) id);
        return director;
    }

    public Director update(Director director) {
        update(UPDATE, director.getName(), director.getId());
        return director;
    }

    public boolean deleteById(int id) {
        return delete(DELETE, id);
    }

    public List<Director> findAllByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = String.format(FIND_ALL_BY_IDS, placeholders);
        return jdbc.query(sql, mapper, ids.toArray());
    }
}
