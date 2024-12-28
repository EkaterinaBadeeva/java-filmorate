package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.mappers.RatingMapper;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    //GET /mpa
    @GetMapping
    public List<RatingDto> findAllGenre() {
        log.info("Получение всех рейтингов.");
        List<Rating> ratings = ratingService.findAllRating();
        return ratings.stream()
                .map(RatingMapper::mapToRatingDto)
                .collect(Collectors.toList());
    }

    //GET /mpa/{id}
    @GetMapping("/{id}")
    public RatingDto findGenreById(@PathVariable int id) {
        log.info("Получение рейтинга по id.");
        Rating rating = ratingService.findRatingById(id);
        return RatingMapper.mapToRatingDto(rating);
    }
}
