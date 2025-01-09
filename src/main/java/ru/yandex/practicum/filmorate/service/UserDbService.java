package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.CommonException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class UserDbService implements UserService {
    private final UserStorage userStorage;
    private final UserRowMapper userRowMapper;

    public Collection<UserDto> findAllUsers() {
        log.info("Получение списка всех пользователей.");
        return userStorage.getAllUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto findUserById(Long id) {
        log.info("Получение пользователя по Id.");
        return userStorage.getUserById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public UserDto create(UserDto dto) {
        log.info("Добавление пользователя.");
        User user = UserMapper.mapToUser(dto);
        checkConditions(user);
        user = userStorage.create(user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto update(UserDto userDto) {
        log.info("Обновление пользователя.");
        User user = UserMapper.mapToUser(userDto);

        if (userStorage.getUserById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с email = " + user.getEmail() + " не найден");
        }

        checkConditions(user);

        user = userStorage.update(user);
        return UserMapper.mapToUserDto(user);
    }

    //PUT /users/{id}/friends/{friendId}
    // добавление в друзья
    public UserDto addUserInFriends(Long id, Long friendId) {
        log.info("Добавление пользователя в друзья.");

        // проверяем необходимые условия
        checkId(id);
        checkId(friendId);

        checkEqualsIds(id, friendId);

        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        if (user.getFriends().contains(friendId)) {
            throw new CommonException("Вы уже добавили этого пользователя в друзья");
        }

        userStorage.addUserInFriends(id, friendId);

        userRowMapper.setFriendsOfUser(user);

        return UserMapper.mapToUserDto(friend);
    }

    //DELETE /users/{id}/friends/{friendId}
    // удаление из друзей
    public UserDto deleteUserFromFriends(Long id, Long friendId) {
        log.info("Удаление пользователя из друзей.");

        // проверяем необходимые условия
        checkId(id);
        checkId(friendId);

        checkEqualsIds(id, friendId);

        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        // если пользователь найден и все условия соблюдены, удаляем его из друзей
        if (user.getFriends().contains(friendId)) {
            userStorage.deleteUserFromFriends(id, friendId);
        }

        return userStorage.getUserById(user.getId())
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя.
    public List<UserDto> findAllUsersInFriends(Long id) {
        log.info("Получение списка пользователей, являющихся друзьями пользователя.");

        // проверяем необходимые условия
        checkId(id);

        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));

        // если пользователь найден и все условия соблюдены, то
        // получаем список пользователей, являющихся друзьями пользователя.
        return userStorage.findAllUsersInFriends(id).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем.
    public List<UserDto> findCommonFriends(Long id, Long otherId) {
        log.info("Получение списка друзей, общих с другим пользователем.");

        // проверяем необходимые условия
        checkId(id);
        checkId(otherId);

        checkEqualsIds(id, otherId);

        // если пользователи найдены и все условия соблюдены, то
        // получаем список пользователей, общих с другим пользователем.
        return userStorage.findCommonFriends(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    private void checkId(Long id) {
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
    }

    private void checkEqualsIds(Long id, Long otherId) {
        if (id.equals(otherId)) {
            log.warn("Id пользователей не могут быть одинаковыми");
            throw new ValidationException("Id пользователей не могут быть одинаковыми");
        }
    }

    private void checkConditions(User user) {
        if (user.getName() == null) {
            log.info("Не задано имя пользователя. Будет присвоено значение логина");
            user.setName(user.getLogin());
        }

        if (user.getName().isEmpty()) {
            log.warn("Задано пустое имя пользователя");
            throw new ValidationException("Задано пустое имя пользователя");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
