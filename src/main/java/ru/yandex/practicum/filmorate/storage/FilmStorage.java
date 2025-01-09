package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

    Film create(Film newFilm);

    Film update(Film newFilm);

    Film addUserLike(Long id, Long userId);

    void deleteUserLike(Long id, Long userId);

    List<Film> findBestFilm(Long count);
}
