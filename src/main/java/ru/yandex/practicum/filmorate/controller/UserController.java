package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    //GET /users/{id}
    // получить пользователя по id
    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto userDto) {
        return userService.update(userDto);
    }

    //PUT /users/{id}/friends/{friendId}
    // добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    public UserDto addUserInFriends(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.addUserInFriends(id, friendId);
    }

    //DELETE /users/{id}/friends/{friendId}
    // удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    public UserDto deleteUserFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.deleteUserFromFriends(id, friendId);
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя
    @GetMapping("/{id}/friends")
    public List<UserDto> findAllUsersInFriends(@PathVariable Long id) {
        return userService.findAllUsersInFriends(id);
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.findCommonFriends(id, otherId);
    }
}
