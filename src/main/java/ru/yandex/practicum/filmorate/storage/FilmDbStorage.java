package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.util.*;

@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreDbStorage;


    @Override
    public Collection<Film> getAllFilms() {
        String sqlQuery = "select film_id, film_name, description, release_Date, duration, rating_id " +
                "from films;";
        return jdbcTemplate.query(sqlQuery, filmRowMapper);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sqlQuery = "select film_id, film_name, description, release_Date, duration, rating_id " +
                "from films where film_id = ?;";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film newFilm) {
        String sqlQuery = "insert into films(film_name, description, release_Date, duration, rating_id) " +
                "values (?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});

            stmt.setString(1, newFilm.getName());
            stmt.setString(2, newFilm.getDescription());
            stmt.setString(3, newFilm.getReleaseDate().toString());
            stmt.setInt(4, newFilm.getDuration());

            Rating mpa = newFilm.getMpa();
            if (mpa != null) {
                stmt.setInt(5, mpa.getId());
            } else {
                stmt.setInt(5, 0);
            }

            return stmt;
        }, keyHolder);
        newFilm.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        addGenresInFilm(newFilm.getGenres(), newFilm.getId());

        return newFilm;
    }

    @Override
    public Film update(Film newFilm) {
        String sqlQuery = "UPDATE films SET " +
                "film_name = ?, description = ?, release_Date = ?, duration = ?, rating_id = ? " +
                "where film_id = ?;";

        Rating mpa = newFilm.getMpa();
        Integer ratingId;
        if (mpa != null) {
            ratingId = mpa.getId();
        } else {
            ratingId = 0;
        }

        jdbcTemplate.update(sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                ratingId,
                newFilm.getId());

        addGenresInFilm(newFilm.getGenres(), newFilm.getId());
        filmRowMapper.setLikesOfFilm(newFilm);
        return newFilm;
    }

    public Film addUserLike(Long id, Long userId) {
        String sqlQuery = "insert into likes(film_id, user_id) values (?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setLong(1, id);
            stmt.setLong(2, userId);

            return stmt;
        }, keyHolder);

        Film film = getFilmById(id).orElseThrow();
        filmRowMapper.setLikesOfFilm(film);

        return film;
    }

    public void deleteUserLike(Long id, Long userId) {
        String sqlQuery = "DELETE FROM LIKES WHERE  FILM_ID =? AND USER_ID=?;";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    public List<Film> findBestFilm(Long count) {
        String sqlQuery = "select films.film_id, films.film_name, films.description, films.release_Date, films.duration, films.rating_id " +
                "from films join likes on  likes.film_id = films.film_id " +
                " group by likes.film_id order by count (likes.user_id) desc limit ?;";
        return jdbcTemplate.query(sqlQuery, filmRowMapper, count);
    }

    private void addGenresInFilm(List<Genre> genres, Long filmId) {
        for (Genre genre : genres) {
            Integer genreId = genre.getId();
            genreDbStorage.getGenreById(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр с Id " + genreId + " не найден"));

            String sqlQuery = "insert into film_genre(film_id, genre_id) " +
                    "values (?, ?);";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});

                stmt.setLong(1, filmId);
                stmt.setInt(2, genreId);

                return stmt;
            }, keyHolder);
        }
    }

    public void insertFilmData(String name, String description, String date, Integer duration) {
        String sqlQuery = "insert into films(film_name, description, release_Date, duration) " +
                "values (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, date);
            stmt.setInt(4, duration);

            return stmt;
        }, keyHolder);
    }
}