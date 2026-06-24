package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/data.sql"})
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MpaRatingStorage mpaRatingStorage;

    @Autowired
    private GenreStorage genreStorage;

    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate, mpaRatingStorage, genreStorage);
    }

    @Test
    void testAddFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film saved = filmStorage.addFilm(film);

        assertNotNull(saved.getId());
        assertEquals("Test Film", saved.getName());
    }

    @Test
    void testGetFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        filmStorage.addFilm(film);

        Optional<Film> found = filmStorage.getFilmById(film.getId());

        assertTrue(found.isPresent());
        assertEquals(film.getId(), found.get().getId());
        assertEquals("Test Film", found.get().getName());
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(130);
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertEquals(2, films.size());
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        filmStorage.addFilm(film);

        film.setName("Updated Film");
        film.setDescription("Updated Description");
        Film updated = filmStorage.updateFilm(film);

        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        filmStorage.addFilm(film);

        filmStorage.deleteFilm(film.getId());

        Optional<Film> found = filmStorage.getFilmById(film.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFilmExists() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        filmStorage.addFilm(film);

        assertTrue(filmStorage.filmExists(film.getId()));
        assertFalse(filmStorage.filmExists(999));
    }
}