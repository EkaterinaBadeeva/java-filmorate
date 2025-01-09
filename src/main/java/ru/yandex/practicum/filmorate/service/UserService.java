package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.UserDto;

import java.util.Collection;
import java.util.List;


public interface UserService {

    Collection<UserDto> findAllUsers();

    UserDto findUserById(Long id);

    UserDto create(UserDto dto);

    UserDto update(UserDto userDto);

    UserDto addUserInFriends(Long id, Long friendId);

    UserDto deleteUserFromFriends(Long id, Long friendId);

    List<UserDto> findAllUsersInFriends(Long id);

    List<UserDto> findCommonFriends(Long id, Long otherId);
}
