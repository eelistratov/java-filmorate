package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = new RowMapper<Film>() {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("film_id"));
            film.setName(rs.getString("film_name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // Загружаем MPA из JOIN
            int mpaId = rs.getInt("mpa_id");
            if (mpaId != 0) {
                MpaRating mpa = new MpaRating();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                mpa.setDescription(rs.getString("mpa_description"));
                film.setMpa(mpa);
            }

            return film;
        }
    };

    @Override
    public List<Film> getAllFilms() {
        // JOIN с mpa_ratings для получения всех данных одним запросом
        String sql = "SELECT f.*, " +
                "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        // Загружаем жанры для каждого фильма отдельно (можно оптимизировать, но для простоты оставляем)
        for (Film film : films) {
            Set<Genre> genres = getGenresByFilmId(film.getId());
            film.setGenres(genres);
        }

        return films;
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT f.*, " +
                "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);

        // Загружаем жанры для найденного фильма
        films.forEach(film -> {
            Set<Genre> genres = getGenresByFilmId(film.getId());
            film.setGenres(genres);
        });

        return films.stream().findFirst();
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (film_name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, mpaId);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveFilmGenres(film);
        log.debug("Фильм добавлен с id {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET film_name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE film_id = ?";

        Integer mpaId = film.getMpa() != null ? film.getMpa().getId() : null;

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        updateFilmGenres(film);

        log.debug("Фильм с id {} обновлён", film.getId());
        return film;
    }

    @Override
    public void deleteFilm(Integer id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        log.debug("Фильм с id {} удалён", id);
    }

    @Override
    public boolean filmExists(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Set<Genre> getGenresByFilmId(Integer filmId) {
        String sql = "SELECT g.genre_id, g.genre_name FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.genre_id";

        List<Genre> genreList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            return genre;
        }, filmId);

        return new HashSet<>(genreList);
    }

    /**
     * Сохраняет жанры фильма с использованием пакетной вставки (batchUpdate)
     * для повышения производительности.
     */
    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Genre> genres = new ArrayList<>(film.getGenres());

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Genre genre = genres.get(i);
                        ps.setInt(1, film.getId());
                        ps.setInt(2, genre.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                }
        );
    }

    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveFilmGenres(film);
    }
}