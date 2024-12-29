package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> findFilmById(Long id);

    Film getFilmById(Long id);

    Film create(Film newFilm);

    Film update(Film newFilm);
}
