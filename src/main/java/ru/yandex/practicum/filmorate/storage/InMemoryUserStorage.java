package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User addUser(User user) {
        user.setId(nextId++);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено из логина: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище с id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден в хранилище", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено из логина: {}", user.getLogin());
        }

        users.put(user.getId(), user);
        log.debug("Пользователь с id {} обновлён в хранилище", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
        log.debug("Пользователь с id {} удалён из хранилища", id);
    }

    @Override
    public boolean userExists(Integer id) {
        return users.containsKey(id);
    }
}