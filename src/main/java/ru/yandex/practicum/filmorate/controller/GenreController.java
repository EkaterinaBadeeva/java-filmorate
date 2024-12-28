package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    //GET /genres
    @GetMapping
    public List<GenreDto> findAllGenre() {
        log.info("Получение всех жанров.");
        List<Genre> genres = genreService.findAllGenre();
        return genres.stream()
                .map(GenreMapper::mapToGenreDto)
                .collect(Collectors.toList());
    }

    //GET /genres/{id}
    @GetMapping("/{id}")
    public GenreDto findGenreById(@PathVariable int id) {
        log.info("Получение жанра по id.");
        Genre genre = genreService.findGenreById(id);
        return GenreMapper.mapToGenreDto(genre);
    }
}
