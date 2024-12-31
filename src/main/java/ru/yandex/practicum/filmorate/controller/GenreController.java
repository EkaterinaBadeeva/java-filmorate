package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    //GET /genres
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GenreDto> findAllGenre() {
        return genreService.findAllGenre();
    }

    //GET /genres/{id}
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GenreDto findGenreById(@PathVariable int id) {
        return genreService.findGenreById(id);
    }
}
