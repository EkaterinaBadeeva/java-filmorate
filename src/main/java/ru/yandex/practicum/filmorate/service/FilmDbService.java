package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CommonException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Primary
@Service
public class FilmDbService implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmDbService(FilmStorage filmStorage, UserStorage userStorage, JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    @Override
    public Film addUserLike(Long id, Long userId) {
        log.info("Добавление лайка фильму от пользователя.");

        checkId(id);
        checkId(userId);

        Film film = filmStorage.findFilmById(id);
        User user = userStorage.findUserById(userId);

        if (film.getLikes().contains(userId)) {
            throw new CommonException("Вы уже поставили лайк этому фильму. Мы рады, что он вам так понравился");
        }

        String sqlQuery = "insert into likes(film_id, user_id) values (?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setLong(1, id);
            stmt.setLong(2, userId);

            return stmt;
        }, keyHolder);

        filmRowMapper.setLikesOfFilm(film);
        return film;
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    @Override
    public Film deleteUserLike(Long id, Long userId) {
        log.info("Удаление лайка фильму от пользователя.");

        checkId(id);
        checkId(userId);

        Film film = filmStorage.findFilmById(id);
        User user = userStorage.findUserById(userId);
        if (film.getLikes().contains(userId)) {

            String sqlQuery = "DELETE FROM LIKES WHERE  FILM_ID =? AND USER_ID=?;";

            jdbcTemplate.update(sqlQuery, id, userId);

        }
        return filmStorage.findFilmById(film.getId());
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возвращает первые 10
    @Override
    public List<Film> findBestFilm(Long count) {
        log.info(MessageFormat.format("Возвращает список из первых {0} фильмов по количеству лайков", count));
        String sqlQuery = "select films.film_id, films.film_name, films.description, films.release_Date, films.duration, films.rating_id " +
                "from films join likes on  likes.film_id = films.film_id " +
                " group by likes.film_id order by count (likes.user_id) desc limit ?;";
        List<Film> listOfBestFilms = jdbcTemplate.query(sqlQuery, filmRowMapper, count);
        return listOfBestFilms;
    }

    private void checkId(Long id) {
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
    }
}
