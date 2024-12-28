package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service

public class InMemoryUserService implements UserService {

    private final UserStorage userStorage;

    public InMemoryUserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    //PUT /users/{id}/friends/{friendId}
    // добавление в друзья
    public User addUserInFriends(Long id, Long friendId) {
        log.info("Добавление пользователя в друзья.");

        // проверяем необходимые условия
        checkId(id);
        checkId(friendId);

        checkEqualsIds(id, friendId);

        User user = userStorage.findUserById(id);
        User friend = userStorage.findUserById(friendId);

        // если пользователь найден и все условия соблюдены, добавляем его в друзья
        Set<Long> userFriends = user.getFriends();
        if (userFriends == null) {
            userFriends = new HashSet<Long>();
        }

        userFriends.add(friendId);
        user.setFriends(userFriends);

        Set<Long> friendFriends = friend.getFriends();
        if (friendFriends == null) {
            friendFriends = new HashSet<Long>();
        }
        friendFriends.add(id);
        friend.setFriends(friendFriends);

        return user;
    }

    //DELETE /users/{id}/friends/{friendId}
    // удаление из друзей
    public User deleteUserFromFriends(Long id, Long friendId) {
        log.info("Удаление пользователя из друзей.");

        // проверяем необходимые условия
        checkId(id);
        checkId(friendId);

        checkEqualsIds(id, friendId);

        User user = userStorage.findUserById(id);
        User friend = userStorage.findUserById(friendId);

        // если пользователь найден и все условия соблюдены, удаляем его из друзей
        Set<Long> userFriends = user.getFriends();

        userFriends.remove(friendId);
        user.setFriends(userFriends);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.remove(id);
        friend.setFriends(friendFriends);

        return user;
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя.
    public List<User> findAllUsersInFriends(Long id) {
        log.info("Получение списка пользователей, являющихся друзьями пользователя.");

        // проверяем необходимые условия
        checkId(id);

        User user = userStorage.findUserById(id);
        Set<Long> idsFriends = user.getFriends();

        // если пользователь найден и все условия соблюдены, то
        // получаем список пользователей, являющихся друзьями пользователя.
        Set<User> friends = new HashSet<>();
        for (Long idFriend : idsFriends) {
            User friend = userStorage.findUserById(idFriend);
            friends.add(friend);
        }
        return (List<User>) friends;
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем.
    public List<User> findCommonFriends(Long id, Long otherId) {
        log.info("Получение списка друзей, общих с другим пользователем.");

        // проверяем необходимые условия
        checkId(id);
        checkId(otherId);

        checkEqualsIds(id, otherId);

        User user = userStorage.findUserById(id);
        User otherUser = userStorage.findUserById(otherId);

        // если пользователи найдены и все условия соблюдены, то
        // получаем список пользователей, общих с другим пользователем.
        Set<User> commonFriends = new HashSet<>();

        for (Long idFriend : user.getFriends()) {
            for (Long idOtherUserFriend : otherUser.getFriends()) {
                if (Objects.equals(idFriend, idOtherUserFriend)) {
                    commonFriends.add(userStorage.findUserById(idFriend));
                }
            }
        }

        return (List<User>) commonFriends;
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
}
