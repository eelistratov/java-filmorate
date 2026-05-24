package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@RestController
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        log.info("Получен запрос GET /users");
        return new ArrayList<>(users.values());
    }

    @PostMapping("/users")
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users с телом: {}", user);

        // Логика: если имя пустое — используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, установлено значение из логина: {}", user.getLogin());
        }

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен с id {}", user.getId());
        return user;
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос PUT /users с телом: {}", user);

        if (user.getId() == null || user.getId() <= 0) {
            log.error("ID пользователя не указан или некорректен: {}", user.getId());
            throw new ValidationException("ID пользователя должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        // Логика: если имя пустое — используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, установлено значение из логина: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} обновлён", user.getId());
        return user;
    }
}