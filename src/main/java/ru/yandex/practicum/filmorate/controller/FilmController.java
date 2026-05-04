package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;
import java.time.LocalDate;

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
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films с телом: {}", film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен с id {}", film.getId());
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films с телом: {}", film);

        if (film.getId() == null || film.getId() <= 0) {
            log.error("ID фильма не указан или некорректен: {}", film.getId());
            throw new ValidationException("ID фильма должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм с id {} обновлён", film.getId());
        return film;
    }
}