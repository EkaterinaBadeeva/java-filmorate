package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {

    Film addUserLike(Long id, Long userId);

    Film deleteUserLike(Long id, Long userId);

    List<Film> findBestFilm(Long count);
}
