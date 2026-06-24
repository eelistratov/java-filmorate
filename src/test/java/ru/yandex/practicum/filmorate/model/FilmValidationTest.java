package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации фильмов (аннотации)")
class FilmValidationTest {

    private static Validator validator;
    private FilmController filmController;
    private Film validFilm;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        List<Film> filmStorageList = new ArrayList<>();
        FilmStorage filmStorage = new FilmStorage() {
            @Override
            public List<Film> getAllFilms() {
                return new ArrayList<>(filmStorageList);
            }

            @Override
            public Optional<Film> getFilmById(Integer id) {
                return filmStorageList.stream()
                        .filter(f -> f.getId().equals(id))
                        .findFirst();
            }

            @Override
            public Film addFilm(Film film) {
                film.setId(filmStorageList.size() + 1);
                filmStorageList.add(film);
                return film;
            }

            @Override
            public Film updateFilm(Film film) {
                return filmStorageList.stream()
                        .filter(f -> f.getId().equals(film.getId()))
                        .findFirst()
                        .map(f -> {
                            f.setName(film.getName());
                            f.setDescription(film.getDescription());
                            f.setReleaseDate(film.getReleaseDate());
                            f.setDuration(film.getDuration());
                            return f;
                        })
                        .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));
            }

            @Override
            public void deleteFilm(Integer id) {
                filmStorageList.removeIf(f -> f.getId().equals(id));
            }

            @Override
            public boolean filmExists(Integer id) {
                return filmStorageList.stream().anyMatch(f -> f.getId().equals(id));
            }
        };

        UserStorage userStorage = new UserStorage() {
            @Override
            public List<User> getAllUsers() {
                return new ArrayList<>();
            }

            @Override
            public Optional<User> getUserById(Integer id) {
                return Optional.empty();
            }

            @Override
            public User addUser(User user) {
                return user;
            }

            @Override
            public User updateUser(User user) {
                return user;
            }

            @Override
            public void deleteUser(Integer id) {
            }

            @Override
            public boolean userExists(Integer id) {
                return false;
            }
        };

        FilmService filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);

        validFilm = new Film();
        validFilm.setName("Inception");
        validFilm.setDescription("A mind-bending thriller");
        validFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        validFilm.setDuration(148);
    }

    @Test
    @DisplayName("Должен пройти валидацию с корректными данными")
    void validFilmShouldPassValidation() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен отклонить фильм с пустым названием")
    void blankNameShouldFailValidation() {
        validFilm.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен отклонить фильм с названием из пробелов")
    void whitespaceNameShouldFailValidation() {
        validFilm.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен отклонить фильм с null названием")
    void nullNameShouldFailValidation() {
        validFilm.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен создать фильм с описанием ровно 200 символов")
    void descriptionExactly200CharsShouldPassValidation() {
        validFilm.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен отклонить фильм с описанием длиннее 200 символов")
    void descriptionLongerThan200ShouldFailValidation() {
        validFilm.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Описание фильма не может быть длиннее 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен создать фильм с пустым описанием")
    void emptyDescriptionShouldPassValidation() {
        validFilm.setDescription("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен создать фильм с датой релиза 28.12.1895")
    void minReleaseDateShouldPassValidation() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен отклонить фильм с датой релиза 27.12.1895")
    void releaseDateBeforeMinShouldFailValidation() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен отклонить фильм с null датой релиза")
    void nullReleaseDateShouldFailValidation() {
        validFilm.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза должна быть указана", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен создать фильм с положительной продолжительностью")
    void positiveDurationShouldPassValidation() {
        validFilm.setDuration(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен отклонить фильм с продолжительностью 0")
    void zeroDurationShouldFailValidation() {
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Должен отклонить фильм с отрицательной продолжительностью")
    void negativeDurationShouldFailValidation() {
        validFilm.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("POST /films - должен создать фильм с корректными данными")
    void addFilmShouldSucceed() {
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
        assertNotNull(validFilm.getId());
    }

    @Test
    @DisplayName("PUT /films - должен обновить существующий фильм")
    void updateExistingFilmShouldSucceed() {
        filmController.addFilm(validFilm);
        int id = validFilm.getId();

        Film updatedFilm = new Film();
        updatedFilm.setId(id);
        updatedFilm.setName("Updated Name");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        updatedFilm.setDuration(150);

        Film result = filmController.updateFilm(updatedFilm);
        assertEquals("Updated Name", result.getName());
        assertEquals(150, result.getDuration());
    }

    @Test
    @DisplayName("PUT /films - должен отклонить обновление с несуществующим id")
    void updateNonExistentFilmShouldFail() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999);
        nonExistentFilm.setName("Test");
        nonExistentFilm.setDescription("Test");
        nonExistentFilm.setReleaseDate(LocalDate.now());
        nonExistentFilm.setDuration(100);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(nonExistentFilm));
        assertEquals("Фильм с id 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("GET /films - должен вернуть пустой список в начале")
    void getAllFilmsInitiallyEmpty() {
        assertTrue(filmController.getAllFilms().isEmpty());
    }

    @Test
    @DisplayName("GET /films - должен вернуть список после добавления")
    void getAllFilmsReturnsAddedFilms() {
        filmController.addFilm(validFilm);
        assertEquals(1, filmController.getAllFilms().size());
    }
}