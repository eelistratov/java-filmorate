package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<MpaRating> mpaRowMapper = (rs, rowNum) -> {
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    };

    @Override
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> getMpaRatingById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, mpaRowMapper, id);
        return ratings.stream().findFirst();
    }

    @Override
    public boolean mpaExists(Integer id) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}