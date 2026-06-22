package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;  // ← Добавьте этот импорт
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAllUsers();

    Optional<User> getUserById(Integer id);

    User addUser(User user);

    User updateUser(User user);

    void deleteUser(Integer id);

    boolean userExists(Integer id);
}