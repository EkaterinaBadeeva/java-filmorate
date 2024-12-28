package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей.");
        String sqlQuery = "select user_id, email, user_login, user_name, birthday " +
                "from users;";
        Collection<User> users = jdbcTemplate.query(sqlQuery, userRowMapper);
        return users;
    }

    @Override
    public User findUserById(Long id) {
        log.info("Получение пользователя по Id.");
        String sqlQuery = "select user_id, email, user_login, user_name, birthday " +
                "from users where user_id = ?;";
        User user;
        try {
            user = jdbcTemplate.queryForObject(sqlQuery, userRowMapper, id);
            return user;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь с Id " + id + " не найден");
        }
    }

    @Override
    public User create(User user) {
        log.info("Добавление пользователя.");
        String sqlQuery = "insert into users(email, user_login, user_name, birthday) " +
                "values ( ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            checkConditions(user);
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
    public User update(User newUser) {
        log.info("Обновление пользователя.");

        String sqlQuery = "UPDATE users SET " +
                "email = ?, user_login = ?, user_name = ?, birthday = ? " +
                "where user_id = ?;";
//        UPDATE users SET  email = 'mail@mail.ru',
//        user_login = 'clore', user_name = 'Иванов Иван Иванович',
//        birthday = '1946-08-20' where user_id = 1;

        if (findUserById(newUser.getId()) == null) {
            throw new NotFoundException("Пользователь с email = " + newUser.getEmail() + " не найден");
        }

        checkConditions(newUser);

        jdbcTemplate.update(sqlQuery
                , newUser.getEmail()
                , newUser.getLogin()
                , newUser.getName()
                , newUser.getBirthday()
                , newUser.getId());

        if (newUser.getId() == 0) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        userRowMapper.setFriendsOfUser(newUser);

        return newUser;
    }

    private void checkConditions(User user) {
        if (user.getName() == null) {
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
