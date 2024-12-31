package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> getAllGenre() {
        String sqlQuery = "select genre_id, genre_name " +
                "from genre;";
        return jdbcTemplate.query(sqlQuery, genreRowMapper);
    }

    public Optional<Genre> getGenreById(int id) {
        String sqlQuery = "select * " +
                "from genre where genre_id = ?;";
        try {
            Genre genre = jdbcTemplate.queryForObject(sqlQuery, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenresOfFilm(Long id) {
        String sqlQueryGenres = "SELECT genre.genre_id, genre.genre_name FROM FILM_GENRE " +
                "join genre on genre.genre_id = film_genre.genre_id " +
                "where film_genre.film_id = ?;";

        return jdbcTemplate.query(sqlQueryGenres, genreRowMapper, id);
    }
}