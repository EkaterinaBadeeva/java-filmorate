package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserControllerTests {
    @Autowired
    UserController userController;

    @BeforeEach
    public void BeforeEach() {
        userController.findAll().clear();
        User user = new User();
        user.setId(Long.valueOf(1));
        user.setEmail("testEmail@yandex.ru");
        user.setName("Test User");
        user.setLogin("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user);
    }

    @Test
    void shouldGetAllUsers() {
        //prepare
        User user2 = new User();
        user2.setId(Long.valueOf(2));
        user2.setEmail("testEmail2@yandex.ru");
        user2.setName("Test User2");
        user2.setLogin("User2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user2);

        //do
        Collection<User> users = userController.findAll();

        //check
        assertEquals(2, users.size(), "Некорректное количество пользователей");
    }

    @Test
    void shouldCreateUser() throws IOException, InterruptedException {
        //prepare
        User newUser = new User();
        newUser.setId(Long.valueOf(2));
        newUser.setEmail("testNewEmail@yandex.ru");
        newUser.setName("Test newUser");
        newUser.setLogin("newUser");
        newUser.setBirthday(LocalDate.of(2005, 1, 1));

        //do
        User createdUser = userController.create(newUser);

        //check
        assertNotNull(createdUser.getId(), "Некоректный Id, Id нового пользователя равен null");
        assertEquals(2, userController.findAll().size(), "Некорректное количество пользователей");
        assertEquals("Test newUser", createdUser.getName(), "Некорректное имя пользователя");
    }

    @Test
    void shouldUpdateUser() {
        //prepare
        User user2 = new User();
        user2.setId(Long.valueOf(2));
        user2.setEmail("testEmail2@yandex.ru");
        user2.setName("Test User2");
        user2.setLogin("User2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user2);

        User testUser = new User();
        testUser.setId(user2.getId());
        testUser.setEmail("testUpdatedEmail@yandex.ru");
        testUser.setName("Test updatedUser");
        testUser.setLogin("updatedUser");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));

        //do
        User updatedUser = userController.update(testUser);

        //check
        assertNotNull(updatedUser.getId(), "Некоректный Id, Id обновленного пользователя равен null");
        assertEquals(user2.getId(), updatedUser.getId(), "Некоректный Id обновленного пользователя");
        assertEquals(2, userController.findAll().size(), "Некорректное количество пользователей");
        assertEquals("Test updatedUser", updatedUser.getName(), "Некорректное имя пользователя");
    }
}
