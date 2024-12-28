package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserService {

   User addUserInFriends(Long id, Long friendId);

    public User deleteUserFromFriends(Long id, Long friendId);

    public List<User> findAllUsersInFriends(Long id);

    public List<User> findCommonFriends(Long id, Long otherId);
}
