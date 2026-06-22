package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/data.sql"})
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testAddUser() {
        User user = new User();
        user.setEmail("test2@example.com");  // ← другой email
        user.setLogin("testuser2");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User saved = userStorage.addUser(user);

        assertNotNull(saved.getId());
        assertEquals("test2@example.com", saved.getEmail());
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("test3@example.com");  // ← другой email
        user.setLogin("testuser3");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        Optional<User> found = userStorage.getUserById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals("test3@example.com", found.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        // Удаляем тестового пользователя, чтобы не дублировать
        jdbcTemplate.update("DELETE FROM users WHERE email = 'test@example.com'");

        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 2, 2));
        userStorage.addUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("test4@example.com");  // ← другой email
        user.setLogin("testuser4");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        user.setName("Updated Name");
        user.setEmail("updated4@example.com");
        User updated = userStorage.updateUser(user);

        assertEquals("Updated Name", updated.getName());
        assertEquals("updated4@example.com", updated.getEmail());

        Optional<User> found = userStorage.getUserById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("test5@example.com");  // ← другой email
        user.setLogin("testuser5");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        userStorage.deleteUser(user.getId());

        Optional<User> found = userStorage.getUserById(user.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testUserExists() {
        User user = new User();
        user.setEmail("test6@example.com");  // ← другой email
        user.setLogin("testuser6");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        assertTrue(userStorage.userExists(user.getId()));
        assertFalse(userStorage.userExists(999));
    }
}