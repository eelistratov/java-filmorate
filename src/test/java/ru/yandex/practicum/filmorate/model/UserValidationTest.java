package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации пользователей (аннотации)")
class UserValidationTest {

    private static Validator validator;
    private UserController userController;
    private User validUser;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        List<User> userStorageList = new ArrayList<>();
        UserStorage userStorage = new UserStorage() {
            @Override
            public List<User> getAllUsers() {
                return new ArrayList<>(userStorageList);
            }

            @Override
            public Optional<User> getUserById(Integer id) {
                return userStorageList.stream()
                        .filter(u -> u.getId().equals(id))
                        .findFirst();
            }

            @Override
            public User addUser(User user) {
                user.setId(userStorageList.size() + 1);
                userStorageList.add(user);
                return user;
            }

            @Override
            public User updateUser(User user) {
                return userStorageList.stream()
                        .filter(u -> u.getId().equals(user.getId()))
                        .findFirst()
                        .map(u -> {
                            u.setEmail(user.getEmail());
                            u.setLogin(user.getLogin());
                            u.setName(user.getName());
                            u.setBirthday(user.getBirthday());
                            return u;
                        })
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + user.getId() + " не найден"));
            }

            @Override
            public void deleteUser(Integer id) {
                userStorageList.removeIf(u -> u.getId().equals(id));
            }

            @Override
            public boolean userExists(Integer id) {
                return userStorageList.stream().anyMatch(u -> u.getId().equals(id));
            }
        };

        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        validUser = new User();
        validUser.setEmail("user@example.com");
        validUser.setLogin("user123");
        validUser.setName("Ivan Petrov");
        validUser.setBirthday(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Должен пройти валидацию с корректными данными")
    void validUserShouldPassValidation() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен отклонить пользователя с пустым email")
    void blankEmailShouldFailValidation() {
        validUser.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email не может быть пустым"));
        assertTrue(hasMessage);
    }

    @Test
    @DisplayName("Должен отклонить пользователя с email без @")
    void emailWithoutAtShouldFailValidation() {
        validUser.setEmail("userexample.com");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email должен содержать символ @"));
        assertTrue(hasMessage);
    }

    @Test
    @DisplayName("Должен отклонить пользователя с пустым логином")
    void blankLoginShouldFailValidation() {
        validUser.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasBlankMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Логин не может быть пустым"));
        assertTrue(hasBlankMessage);
    }

    @Test
    @DisplayName("Должен отклонить пользователя с логином, содержащим пробелы")
    void loginWithSpacesShouldFailValidation() {
        validUser.setLogin("user 123");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Логин не может содержать пробелы"));
        assertTrue(hasMessage);
    }

    @Test
    @DisplayName("Должен отклонить пользователя с датой рождения в будущем")
    void birthdayInFutureShouldFailValidation() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Дата рождения не может быть в будущем"));
        assertTrue(hasMessage);
    }

    @Test
    @DisplayName("Должен отклонить пользователя с null датой рождения")
    void nullBirthdayShouldFailValidation() {
        validUser.setBirthday(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        boolean hasMessage = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Дата рождения должна быть указана"));
        assertTrue(hasMessage);
    }

    @Test
    @DisplayName("Должен создать пользователя с сегодняшней датой рождения")
    void todayBirthdayShouldPassValidation() {
        validUser.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен использовать логин вместо пустого имени")
    void emptyNameShouldUseLogin() {
        validUser.setName("");
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("Должен использовать логин вместо имени из пробелов")
    void blankNameShouldUseLogin() {
        validUser.setName("   ");
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("Должен использовать логин при null имени")
    void nullNameShouldUseLogin() {
        validUser.setName(null);
        User created = userController.addUser(validUser);
        assertEquals("user123", created.getName());
    }

    @Test
    @DisplayName("Должен сохранить имя, если оно указано")
    void validNameShouldKeepIt() {
        validUser.setName("Ivan Ivanov");
        User created = userController.addUser(validUser);
        assertEquals("Ivan Ivanov", created.getName());
    }

    @Test
    @DisplayName("POST /users - должен создать пользователя с корректными данными")
    void addUserShouldSucceed() {
        assertDoesNotThrow(() -> userController.addUser(validUser));
        assertNotNull(validUser.getId());
    }

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

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.updateUser(nonExistentUser));
        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
    }

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