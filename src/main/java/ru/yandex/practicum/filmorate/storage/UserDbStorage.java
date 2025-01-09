package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getAllUsers() {
        String sqlQuery = "select user_id, email, user_login, user_name, birthday " +
                "from users;";
        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sqlQuery = "select user_id, email, user_login, user_name, birthday " +
                "from users where user_id = ?;";
        User user;
        try {
            user = jdbcTemplate.queryForObject(sqlQuery, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        String sqlQuery = "insert into users(email, user_login, user_name, birthday) " +
                "values ( ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getBirthday().toString());
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE users SET " +
                "email = ?, user_login = ?, user_name = ?, birthday = ? " +
                "where user_id = ?;";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        userRowMapper.setFriendsOfUser(user);

        return user;
    }

    @Override
    public void addUserInFriends(Long id, Long friendId) {
        String sqlQuery = "insert into friendship(user_id, friend_id) values (?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setLong(1, id);
            stmt.setLong(2, friendId);

            return stmt;
        }, keyHolder);
    }

    @Override
    public void deleteUserFromFriends(Long id, Long friendId) {
        String sqlQuery = "delete from friendship where user_id =? AND friend_id = ?;";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> findAllUsersInFriends(Long id) {
        String sqlQuery = "select users.user_id, users.email, users.user_login, users.user_name, users.birthday " +
                "from users join friendship on users.user_id = friendship.friend_id where friendship.user_id = ?";
        return jdbcTemplate.query(sqlQuery, userRowMapper, id);
    }

    @Override
    public List<User> findCommonFriends(Long id, Long otherId) {
        String sqlQuery = "select u.user_id, u.email, u.user_login, u.user_name, u.birthday " +
                "from friendship as f1 join friendship as f2 on f1.friend_id = f2.friend_id " +
                "join users as u on u.user_id = f1.friend_id " +
                "where f1.user_id = ? and  f2.user_id = ?";
        return jdbcTemplate.query(sqlQuery, userRowMapper, id, otherId);
    }

    public void insertUserData(String email, String login, String name, String date) {
        String sqlQuery = "insert into users(email, user_login, user_name, birthday) " +
                "values ( ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, email);
            stmt.setString(2, login);
            stmt.setString(3, name);
            stmt.setString(4, date);
            return stmt;
        }, keyHolder);
    }
}
