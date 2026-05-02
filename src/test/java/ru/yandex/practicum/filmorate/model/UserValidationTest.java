package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации пользователей (по спецификации Postman)")
class UserValidationTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("user123");
        validUser.setName("Ivan Petrov");
        validUser.setBirthday(LocalDate.of(1990, 5, 15));
    }

    // тест email
    @Test
    @DisplayName("POST /users - должен создать пользователя с корректным email")
    void createUserWithValidEmailShouldSucceed() {
        assertDoesNotThrow(() -> userController.addUser(validUser));
        assertNotNull(validUser.getId());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с пустым email")
    void createUserWithEmptyEmailShouldFail() {
        validUser.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с email без @")
    void createUserWithEmailWithoutAtShouldFail() {
        validUser.setEmail("userexample.com");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Email должен содержать символ @", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с null email")
    void createUserWithNullEmailShouldFail() {
        validUser.setEmail(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    // тест логина
    @Test
    @DisplayName("POST /users - должен создать пользователя с корректным логином")
    void createUserWithValidLoginShouldSucceed() {
        assertDoesNotThrow(() -> userController.addUser(validUser));
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с пустым логином")
    void createUserWithEmptyLoginShouldFail() {
        validUser.setLogin("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с логином, содержащим пробелы")
    void createUserWithLoginContainingSpacesShouldFail() {
        validUser.setLogin("user 123");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с null логином")
    void createUserWithNullLoginShouldFail() {
        validUser.setLogin(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    // тест имени
    @Test
    @DisplayName("POST /users - должен использовать логин вместо пустого имени")
    void createUserWithEmptyNameShouldUseLogin() {
        validUser.setName("");
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("POST /users - должен использовать логин вместо имени из пробелов")
    void createUserWithBlankNameShouldUseLogin() {
        validUser.setName("   ");
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("POST /users - должен использовать логин при null имени")
    void createUserWithNullNameShouldUseLogin() {
        validUser.setName(null);
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("POST /users - должен сохранить имя, если оно указано")
    void createUserWithValidNameShouldKeepIt() {
        validUser.setName("Ivan Ivanov");
        User created = userController.addUser(validUser);
        assertEquals("Ivan Ivanov", created.getName());
    }

    // тест даты рождения
    @Test
    @DisplayName("POST /users - должен создать пользователя с датой рождения в прошлом")
    void createUserWithBirthdayInPastShouldSucceed() {
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
        assertDoesNotThrow(() -> userController.addUser(validUser));
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с датой рождения в будущем")
    void createUserWithBirthdayInFutureShouldFail() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен отклонить пользователя с null датой рождения")
    void createUserWithNullBirthdayShouldFail() {
        validUser.setBirthday(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.addUser(validUser));
        assertEquals("Дата рождения должна быть указана", exception.getMessage());
    }

    @Test
    @DisplayName("POST /users - должен создать пользователя с сегодняшней датой рождения")
    void createUserWithTodayBirthdayShouldSucceed() {
        validUser.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> userController.addUser(validUser));
    }

    // тест обновления
    @Test
    @DisplayName("PUT /users - должен обновить существующего пользователя")
    void updateExistingUserShouldSucceed() {
        userController.addUser(validUser);
        int id = validUser.getId();

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(1995, 10, 20));

        User result = userController.updateUser(updatedUser);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    @DisplayName("PUT /users - должен отклонить обновление с несуществующим id")
    void updateNonExistentUserShouldFail() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999);
        nonExistentUser.setEmail("test@example.com");
        nonExistentUser.setLogin("test");
        nonExistentUser.setName("Test");
        nonExistentUser.setBirthday(LocalDate.now());


        NotFoundException exception = assertThrows(NotFoundException.class,  // ← ИЗМЕНЕНО
                () -> userController.updateUser(nonExistentUser));
        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("PUT /users - должен отклонить обновление с id <= 0")
    void updateUserWithInvalidIdShouldFail() {
        validUser.setId(0);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.updateUser(validUser));
        assertEquals("ID пользователя должен быть указан", exception.getMessage());
    }

    @Test
    @DisplayName("PUT /users - должен обновить имя из пустоты на логин")
    void updateUserWithEmptyNameShouldUseLogin() {
        userController.addUser(validUser);
        int id = validUser.getId();

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setEmail("user@example.com");
        updatedUser.setLogin("user123");
        updatedUser.setName("");
        updatedUser.setBirthday(LocalDate.of(1990, 5, 15));

        User result = userController.updateUser(updatedUser);
        assertEquals("user123", result.getName());
    }

    // тест GET
    @Test
    @DisplayName("GET /users - должен вернуть пустой список в начале")
    void getAllUsersInitiallyEmpty() {
        assertTrue(userController.getAllUsers().isEmpty());
    }

    @Test
    @DisplayName("GET /users - должен вернуть список после добавления")
    void getAllUsersReturnsAddedUsers() {
        userController.addUser(validUser);
        assertEquals(1, userController.getAllUsers().size());
    }
}