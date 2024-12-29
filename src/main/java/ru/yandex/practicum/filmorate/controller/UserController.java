package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@Qualifier("userDbStorage")
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Получение списка всех пользователей.");
        Collection<User> users = userStorage.findAll();
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    //GET /users/{id}
    // получить пользователя по id
    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Long id) {
        log.info("Получение пользователя по id.");
        return userStorage.findUserById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto dto) {
        log.info("Добавление пользователя.");
        User user = UserMapper.mapToUser(dto);
        user = userStorage.create(user);
        return UserMapper.mapToUserDto(user);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto dto) {
        log.info("Обновление пользователя.");
        User newUser = UserMapper.mapToUser(dto);
        newUser = userStorage.update(newUser);
        return UserMapper.mapToUserDto(newUser);
    }

    //PUT /users/{id}/friends/{friendId}
    // добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    public UserDto addUserInFriends(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление пользователя в друзья.");
        User user = userService.addUserInFriends(id, friendId);
        return UserMapper.mapToUserDto(user);
    }

    //DELETE /users/{id}/friends/{friendId}
    // удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    public UserDto deleteUserFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление пользователя из друзей.");
        User user = userService.deleteUserFromFriends(id, friendId);
        return UserMapper.mapToUserDto(user);
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя
    @GetMapping("/{id}/friends")
    public List<UserDto> findAllUsersInFriends(@PathVariable Long id) {
        log.info("Получение списка пользователей, являющихся друзьями пользователя.");
        List<User> users = userService.findAllUsersInFriends(id);
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получение списка друзей, общих с другим пользователем.");
        List<User> users = userService.findCommonFriends(id, otherId);
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}
