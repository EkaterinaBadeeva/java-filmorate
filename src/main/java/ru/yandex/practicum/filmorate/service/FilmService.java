package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    public Film addUserLike(Long id, Long userId) {
        log.info("Добавление лайка фильму от пользователя.");

        // проверяем необходимые условия
        checkId(id);
        checkId(userId);

        Film film = filmStorage.findFilmById(id);
        User user = userStorage.findUserById(userId);

        // если пользователь и фильм найдены и все условия соблюдены, добавляем лайк от пользователя
        Set<Long> userLikes = film.getLikes();
        if (userLikes == null) {
            userLikes = new HashSet<Long>();
        }
        userLikes.add(userId);
        film.setLikes(userLikes);

        return film;
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    public Film deleteUserLike(Long id, Long userId) {
        log.info("Удаление лайка фильму от пользователя.");

        // проверяем необходимые условия
        checkId(id);
        checkId(userId);

        Film film = filmStorage.findFilmById(id);
        User user = userStorage.findUserById(userId);

        // если пользователь и фильм найдены и все условия соблюдены, удаляем лайк от пользователя
        Set<Long> userLikes = film.getLikes();

        userLikes.remove(userId);
        film.setLikes(userLikes);

        return film;
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возвращает первые 10
    public List<Film> findBestFilm(Long count) {

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparing((film) -> film.getLikes().size()))
                .limit(count)
                .toList()
                .reversed();
    }

    private void checkId(Long id) {
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
    }
}
