package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.RatingMapper;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingDbStorage ratingDbStorage;

    public List<RatingDto> findAllRating() {
        log.info("Получение всех рейтингов.");
        return ratingDbStorage.getAllRating().stream()
                .map(RatingMapper::mapToRatingDto)
                .collect(Collectors.toList());
    }

    public RatingDto findRatingById(int id) {
        log.info("Получение рейтинга по id.");
        return ratingDbStorage.getRatingById(id)
                .map(RatingMapper::mapToRatingDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг с Id " + id + " не найден"));
    }

    public Rating getRatingOfFilm(Long id) {
        return ratingDbStorage.getRatingOfFilm(id);
    }
}
