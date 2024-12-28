package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    public List<Rating> findAllRating() {
        log.info("Получение всех рейтингов.");
        String sqlQuery = "select rating_id, rating_name " +
                "from rating;";

        List<Rating> ratings = jdbcTemplate.query(sqlQuery,ratingRowMapper);
        return ratings;
    }

    public Rating findRatingById(int id) {
        log.info("Получение рейтинга по id.");

        String sqlQuery = "select  rating_id, rating_name " +
                "from rating where rating_id = ?;";
        Rating rating;
        try {
            rating = jdbcTemplate.queryForObject(sqlQuery, ratingRowMapper, id);

            return rating;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Рейтинг с Id " + id + " не найден");
        }
    }
}
