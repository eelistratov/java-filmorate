package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации фильмов (по файлу Postman)")
class FilmValidationTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Inception");
        validFilm.setDescription("A mind-bending thriller");
        validFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        validFilm.setDuration(148);
    }

    // тест названия
    @Test
    @DisplayName("POST /films - должен создать фильм с корректным названием")
    void createFilmWithValidNameShouldSucceed() {
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
        assertNotNull(validFilm.getId());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с пустым названием")
    void createFilmWithEmptyNameShouldFail() {
        validFilm.setName("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с названием из пробелов")
    void createFilmWithBlankNameShouldFail() {
        validFilm.setName("   ");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с null названием")
    void createFilmWithNullNameShouldFail() {
        validFilm.setName(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    // тест описания
    @Test
    @DisplayName("POST /films - должен создать фильм с описанием ровно 200 символов")
    void createFilmWithDescriptionExactly200CharsShouldSucceed() {
        validFilm.setDescription("a".repeat(200));
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
        assertEquals(200, validFilm.getDescription().length());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с описанием длиннее 200 символов")
    void createFilmWithDescriptionLongerThan200ShouldFail() {
        validFilm.setDescription("a".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Описание фильма не может быть длиннее 200 символов", exception.getMessage());
    }

    @Test
    @DisplayName("POST /films - должен создать фильм с пустым описанием")
    void createFilmWithEmptyDescriptionShouldSucceed() {
        validFilm.setDescription("");
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    // тест даты релиза
    @Test
    @DisplayName("POST /films - должен создать фильм с датой релиза 28.12.1895")
    void createFilmWithMinReleaseDateShouldSucceed() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с датой релиза 27.12.1895")
    void createFilmWithReleaseDateBeforeMinShouldFail() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с null датой релиза")
    void createFilmWithNullReleaseDateShouldFail() {
        validFilm.setReleaseDate(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    // тест длительности
    @Test
    @DisplayName("POST /films - должен создать фильм с положительной продолжительностью")
    void createFilmWithPositiveDurationShouldSucceed() {
        validFilm.setDuration(1);
        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с продолжительностью 0")
    void createFilmWithZeroDurationShouldFail() {
        validFilm.setDuration(0);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }

    @Test
    @DisplayName("POST /films - должен отклонить фильм с отрицательной продолжительностью")
    void createFilmWithNegativeDurationShouldFail() {
        validFilm.setDuration(-10);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(validFilm));
        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }

    // тест обновления
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(nonExistentFilm));
        assertEquals("Фильм с id 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("PUT /films - должен отклонить обновление с id <= 0")
    void updateFilmWithInvalidIdShouldFail() {
        validFilm.setId(0);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.updateFilm(validFilm));
        assertEquals("ID фильма должен быть указан", exception.getMessage());
    }

    // тест GET
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