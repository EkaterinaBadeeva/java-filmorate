package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public Optional<User> getUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(user);
        }
    }

    public User create(User user) {
        // формируем дополнительные данные
        user.setId(getNextId());

        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            // если пользователь найден и все условия соблюдены, обновляем информацию о нём
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }

            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с email = " + newUser.getEmail() + " не найден");
    }

    @Override
    public void addUserInFriends(Long id, Long friendId) {

        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
        User friend = getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + friendId));

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
    }

    @Override
    public void deleteUserFromFriends(Long id, Long friendId) {

        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
        User friend = getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + friendId));

        // если пользователь найден и все условия соблюдены, удаляем его из друзей
        Set<Long> userFriends = user.getFriends();

        userFriends.remove(friendId);
        user.setFriends(userFriends);

        Set<Long> friendFriends = friend.getFriends();
        friendFriends.remove(id);
        friend.setFriends(friendFriends);
    }

    @Override
    public List<User> findAllUsersInFriends(Long id) {
        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
        ;
        Set<Long> idsFriends = user.getFriends();

        // если пользователь найден и все условия соблюдены, то
        // получаем список пользователей, являющихся друзьями пользователя.
        Set<User> friends = new HashSet<>();
        for (Long idFriend : idsFriends) {
            User friend = getUserById(idFriend)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + idFriend));
            ;
            friends.add(friend);
        }
        return (List<User>) friends;
    }

    @Override
    public List<User> findCommonFriends(Long id, Long otherId) {
        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
        User otherUser = getUserById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + otherId));

        // если пользователи найдены и все условия соблюдены, то
        // получаем список пользователей, общих с другим пользователем.
        Set<User> commonFriends = new HashSet<>();

        for (Long idFriend : user.getFriends()) {
            for (Long idOtherUserFriend : otherUser.getFriends()) {
                if (Objects.equals(idFriend, idOtherUserFriend)) {
                    commonFriends.add(getUserById(idFriend)
                            .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + idFriend)));
                }
            }
        }

        return (List<User>) commonFriends;
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
