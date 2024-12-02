package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findFilmById(Long id);

    Film create(Film newFilm);

    Film update(Film newFilm);
}
