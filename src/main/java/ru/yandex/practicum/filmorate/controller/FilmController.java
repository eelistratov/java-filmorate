package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        log.info("Получен запрос GET /films");
        return new ArrayList<>(films.values());
    }

    @PostMapping("/films")
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос POST /films с телом: {}", film);
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен с id {}", film.getId());
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос PUT /films с телом: {}", film);

        if (film.getId() <= 0) {
            log.error("ID фильма не указан или некорректен: {}", film.getId());
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм с id {} обновлён", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма длиннее 200 символов");
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.error("Ошибка валидации: дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.error("Ошибка валидации: дата релиза {} раньше {}", film.getReleaseDate(), minReleaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность {} <= 0", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        log.debug("Валидация фильма пройдена успешно");
    }
}