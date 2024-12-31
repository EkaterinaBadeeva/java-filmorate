package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User create(User user);

    User update(User newUser);

    void addUserInFriends(Long id, Long friendId);

    void deleteUserFromFriends(Long id, Long friendId);

    List<User> findAllUsersInFriends(Long id);

    List<User> findCommonFriends(Long id, Long otherId);
}
