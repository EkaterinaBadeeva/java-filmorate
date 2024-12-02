package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получение всех фильмов.");
        return filmStorage.findAll();
    }

    //GET /films/{id}
    // получить фильм по id
    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable Long id) {
        log.info("Получение фильма по id.");
        return filmStorage.findFilmById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Добавление фильма.");
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Обновление фильма.");
        return filmStorage.update(newFilm);
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public Film addUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь ставит лайк фильму.");
        return filmService.addUserLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteUserLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь удаляет лайк фильму.");
        return filmService.deleteUserLike(id, userId);
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возращает первые 10
    @GetMapping("/popular")
    public List<Film> findBestFilm(@RequestParam(defaultValue = "10") Long count) {
        log.info(MessageFormat.format("Возвращает список из первых {0} фильмов по количеству лайков", count));
        return filmService.findBestFilm(count);
    }
}
