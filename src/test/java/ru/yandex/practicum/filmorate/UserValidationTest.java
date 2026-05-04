package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userStorage, userService);
    }

    @Test
    void shouldNotAddUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("noat");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void shouldNotAddUserWithLoginContainingSpace() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("bad login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void shouldNotAddUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.addUser(user));
    }

    @Test
    void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("myLogin");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        User created = controller.addUser(user);
        assertEquals("myLogin", created.getName());
    }

    @Test
    void shouldAddValidUser() {
        User user = new User();
        user.setEmail("valid@mail.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> controller.addUser(user));
    }
}