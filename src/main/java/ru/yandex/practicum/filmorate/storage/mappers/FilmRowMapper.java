package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private final RatingRowMapper ratingRowMapper;

    public FilmRowMapper(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper, RatingRowMapper ratingRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
        this.ratingRowMapper = ratingRowMapper;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = Film.builder()
                .id(rs.getLong(1))
                .name(rs.getString(2))
                .description(rs.getString(3))
                .releaseDate(rs.getDate(4).toLocalDate())
                .duration(rs.getInt(5))
                .mpa(setRatingById(rs.getInt(6)))
                .build();

        setLikesOfFilm(film);
        setGenresOfFilm(film);

        return film;
    }

    public void setLikesOfFilm(Film film) {
        String sqlQueryLikes = "SELECT user_id FROM LIKES where film_id = ?;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQueryLikes, film.getId());
        film.setLikes(rows.stream()
                .map(key -> ((Number) key.get("user_id")).longValue())
                .collect(Collectors.toSet())
        );
    }

    public void setGenresOfFilm(Film film) {

        String sqlQueryGenres = "SELECT genre.genre_id, genre.genre_name FROM FILM_GENRE " +
                "join genre on genre.genre_id = film_genre.genre_id " +
                "where film_genre.film_id = ?;";

        List<Genre> genres = jdbcTemplate.query(sqlQueryGenres, genreRowMapper, film.getId());
        film.setGenres(genres);
    }

    public Rating setRatingById(Integer ratingId) {
        if (ratingId == 0) {
            return null;
        }

        String sqlQueryGenres = "SELECT rating_id, rating_name from rating " +
                "where rating_id = ?";
        return jdbcTemplate.queryForObject(sqlQueryGenres, ratingRowMapper, ratingId);
    }
}
