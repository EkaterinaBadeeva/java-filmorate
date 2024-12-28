package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CommonException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Primary
@Service
public class UserDbService implements UserService {

    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDbService(UserStorage userStorage, JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
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

        if (user.getFriends().contains(friendId)) {
            throw new CommonException("Вы уже добавили этого пользователя в друзья");
        }

        // если пользователь найден и все условия соблюдены, добавляем его в друзья
        String sqlQuery = "insert into friendship(user_id, friend_id) values (?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setLong(1, id);
            stmt.setLong(2, friendId);

            return stmt;
        }, keyHolder);

//        String sqlQueryFroFiend = "insert into friendship(user_id, friend_id) values (?, ?);";
//        keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(connection -> {
//            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
//            stmt.setLong(1, friendId);
//            stmt.setLong(2, id);
//
//            return stmt;
//        }, keyHolder);

        userRowMapper.setFriendsOfUser(user);
       // userRowMapper.setFriendsOfUser(friend);
        return friend;
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
        if (user.getFriends().contains(friendId)) {
            String sqlQuery = "delete from friendship where user_id =? AND friend_id = ?;";

            jdbcTemplate.update(sqlQuery, id, friendId);
          //  jdbcTemplate.update(sqlQuery, friendId, id);
        }

        return userStorage.findUserById(user.getId());
    }

    //GET /users/{id}/friends
    // получение списка пользователей, являющихся друзьями пользователя.
    public List<User> findAllUsersInFriends(Long id) {
        log.info("Получение списка пользователей, являющихся друзьями пользователя.");

        // проверяем необходимые условия
        checkId(id);

        User user = userStorage.findUserById(id);

        // если пользователь найден и все условия соблюдены, то
        // получаем список пользователей, являющихся друзьями пользователя.
        String sqlQuery = "select users.user_id, users.email, users.user_login, users.user_name, users.birthday " +
                "from users join friendship on users.user_id = friendship.friend_id where friendship.user_id = ?";
        List<User> friends = jdbcTemplate.query(sqlQuery, userRowMapper, id);

        return friends;
    }

    //GET /users/{id}/friends/common/{otherId}
    // получение списка друзей, общих с другим пользователем.
    public List<User> findCommonFriends(Long id, Long otherId) {
        log.info("Получение списка друзей, общих с другим пользователем.");

        // проверяем необходимые условия
        checkId(id);
        checkId(otherId);

        checkEqualsIds(id, otherId);

        // если пользователи найдены и все условия соблюдены, то
        // получаем список пользователей, общих с другим пользователем.
        String sqlQuery = "select u.user_id, u.email, u.user_login, u.user_name, u.birthday " +
                "from friendship as f1 join friendship as f2 on f1.friend_id = f2.friend_id " +
                "join users as u on u.user_id = f1.friend_id " +
                "where f1.user_id = ? and  f2.user_id = ?";
        List<User> commonFriends = jdbcTemplate.query(sqlQuery, userRowMapper, id, otherId);

        return commonFriends;
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
