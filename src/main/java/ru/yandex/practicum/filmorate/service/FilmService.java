package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.util.Collection;
import java.util.List;

public interface FilmService {

    Collection<FilmDto> findAllFilms();

    FilmDto findFilmById(Long id);

    FilmDto create(FilmDto filmDto);

    FilmDto update(FilmDto filmDto);

    FilmDto addUserLike(Long id, Long userId);

    FilmDto deleteUserLike(Long id, Long userId);

    List<FilmDto> findBestFilm(Long count);
}
