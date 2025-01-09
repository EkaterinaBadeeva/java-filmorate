package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    public List<Rating> getAllRating() {
        String sqlQuery = "select * " +
                "from rating;";
        return jdbcTemplate.query(sqlQuery, ratingRowMapper);
    }

    public Optional<Rating> getRatingById(int id) {
        String sqlQuery = "select  rating_id, rating_name " +
                "from rating where rating_id = ?;";
        try {
            Rating rating = jdbcTemplate.queryForObject(sqlQuery, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Rating setRatingById(Integer ratingId) {
        if (ratingId == 0) {
            return null;
        }

        String sqlQueryGenres = "SELECT rating_id, rating_name from rating " +
                "where rating_id = ?";
        return jdbcTemplate.queryForObject(sqlQueryGenres, ratingRowMapper, ratingId);
    }

    public Rating getRatingOfFilm(Long id) {
        String sqlQueryGenres = "SELECT rating.rating_id, rating.rating_name from rating " +
                "join films on films.rating_id = rating.rating_id " +
                "where films.film_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sqlQueryGenres, ratingRowMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }
}
