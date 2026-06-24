package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaRatingStorage mpaRatingStorage;
    private final GenreStorage genreStorage;

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film addFilm(Film film) {
        // Проверка и загрузка MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating mpa = mpaRatingStorage.getMpaRatingById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг MPA с id " + film.getMpa().getId() + " не найден"
                    ));
            film.setMpa(mpa);
        }

        // Проверка и загрузка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> validGenres = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    Genre found = genreStorage.getGenreById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id " + genre.getId() + " не найден"
                            ));
                    validGenres.add(found);
                }
            }
            film.setGenres(validGenres);
        }

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new NotFoundException("ID фильма должен быть указан");
        }

        // Проверка существования фильма
        if (!filmStorage.filmExists(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        // Проверка и загрузка MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            MpaRating mpa = mpaRatingStorage.getMpaRatingById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг MPA с id " + film.getMpa().getId() + " не найден"
                    ));
            film.setMpa(mpa);
        }

        // Проверка и загрузка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> validGenres = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genre.getId() != null) {
                    Genre found = genreStorage.getGenreById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id " + genre.getId() + " не найден"
                            ));
                    validGenres.add(found);
                }
            }
            film.setGenres(validGenres);
        }

        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Film film = getFilmById(filmId);
        film.addLike(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Film film = getFilmById(filmId);

        if (!film.isLikedByUser(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            return;
        }

        film.removeLike(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getTopPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}