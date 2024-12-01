package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей.");
        return userStorage.findAll();
    }

    //GET /users/{id}
    // получить пользователя по id
    @GetMapping("/{id}")
    public User findUserById(@PathVariable Long id) {
        log.info("Получение пользователя по id.");
        return userStorage.findUserById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Добавление пользователя.");
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Обновление пользователя.");
        return userStorage.update(newUser);
    }

    //PUT /users/{id}/friends/{friendId}
    // добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    public User addUserInFriends(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление пользователя в друзья.");
        return userService.addUserInFriends(id, friendId);
    }

    //DELETE /users/{id}/friends/{friendId}
    // удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteUserFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление пользователя из друзей.");
        return userService.deleteUserFromFriends(id, friendId);
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя
    @GetMapping("/{id}/friends")
    public Set<User> findAllUsersInFriends(@PathVariable Long id) {
        log.info("Получение списка пользователей, являющихся друзьями пользователя.");
        return userService.findAllUsersInFriends(id);
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем
    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получение списка друзей, общих с другим пользователем.");
        return userService.findCommonFriends(id, otherId);
    }
}
