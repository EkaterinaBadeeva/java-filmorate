package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@Qualifier("filmDbStorage")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Получение всех фильмов.");
        Collection<Film> films = filmStorage.findAll();
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    //GET /films/{id}
    // получить фильм по id
    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable Long id) {
        log.info("Получение фильма по id.");
        return filmStorage.findFilmById(id)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        log.info("Добавление фильма.");
        Film film = FilmMapper.mapToFilm(filmDto);
        film = filmStorage.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        log.info("Обновление фильма.");
        Film newFilm = FilmMapper.mapToFilm(filmDto);
        newFilm = filmStorage.update(newFilm);
        return FilmMapper.mapToFilmDto(newFilm);
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public FilmDto addUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь ставит лайк фильму.");
        return FilmMapper.mapToFilmDto(filmService.addUserLike(id, userId));
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь удаляет лайк фильму.");
        Film film = filmService.deleteUserLike(id, userId);
        return FilmMapper.mapToFilmDto(film);
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возращает первые 10
    @GetMapping("/popular")
    public List<FilmDto> findBestFilm(@RequestParam(defaultValue = "10") Long count) {
        log.info(MessageFormat.format("Возвращает список из первых {0} фильмов по количеству лайков", count));
        List<Film> bestFilms = filmService.findBestFilm(count);
        return bestFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
