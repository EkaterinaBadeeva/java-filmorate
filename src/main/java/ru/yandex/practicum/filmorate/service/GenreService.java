package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> findAllGenre() {
        log.info("Получение всех жанров.");
        String sqlQuery = "select genre_id, genre_name " +
                "from genre;";

        List<Genre> genres = jdbcTemplate.query(sqlQuery, genreRowMapper);
        return genres;
    }

    public Genre findGenreById(int id) {
        log.info("Получение жанра по id.");

        String sqlQuery = "select  genre_id, genre_name " +
                "from genre where genre_id = ?;";
        Genre genre;
        try {
            genre = jdbcTemplate.queryForObject(sqlQuery, genreRowMapper, id);

            return genre;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Жанр с Id " + id + " не найден");
        }
    }
}
