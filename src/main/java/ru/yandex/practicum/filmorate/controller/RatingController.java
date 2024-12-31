package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    //GET /mpa
    @GetMapping
    public List<RatingDto> findAllGenre() {
        return ratingService.findAllRating();
    }

    //GET /mpa/{id}
    @GetMapping("/{id}")
    public RatingDto findGenreById(@PathVariable int id) {
        return ratingService.findRatingById(id);
    }
}
