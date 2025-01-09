package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        return filmService.findAllFilms();
    }

    //GET /films/{id}
    // получить фильм по id
    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable Long id) {
        return filmService.findFilmById(id);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        return filmService.create(filmDto);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        return filmService.update(filmDto);
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public FilmDto addUserLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addUserLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteUserLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteUserLike(id, userId);
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возращает первые 10
    @GetMapping("/popular")
    public List<FilmDto> findBestFilm(@RequestParam(defaultValue = "10") Long count) {
        return filmService.findBestFilm(count);
    }
}
