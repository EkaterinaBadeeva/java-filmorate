package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreService genreService;
    private final RatingService ratingService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        log.info("Получение всех фильмов.");
        String sqlQuery = "select film_id, film_name, description, release_Date, duration, rating_id " +
                "from films;";
        Collection<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper);
        return films;
    }

    @Override
    public Film findFilmById(Long id) {
        log.info("Получение фильма по id.");
        String sqlQuery = "select film_id, film_name, description, release_Date, duration, rating_id " +
                "from films where film_id = ?;";
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);

            return film;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Фильм с Id " + id + " не найден");
        }
    }

    @Override
    public Film create(Film newFilm) {
        log.info("Добавление фильма.");

        try {
            Rating mpa = newFilm.getMpa();
            if (mpa != null) {
                ratingService.findRatingById(mpa.getId());
            }

            List<Genre> genres = newFilm.getGenres();
            if (genres != null) {
                Set<Genre> setGenre = new LinkedHashSet<>(genres);
                genres = new ArrayList<>(setGenre);

                for (Genre genre : genres) {
                    if (genre != null) {
                        genreService.findGenreById(genre.getId());
                    }
                }

                newFilm.setGenres(genres);
            } else {
                newFilm.setGenres(new ArrayList<>());
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }


        String sqlQuery = "insert into films(film_name, description, release_Date, duration, rating_id) " +
                "values (?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            if (newFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.warn("Указанна дата релиза раньше 28 декабря 1895 года");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }

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
        log.info("Обновление фильма.");

        try {
            Rating mpa = newFilm.getMpa();
            if (mpa != null) {
                ratingService.findRatingById(mpa.getId());
            }

            List<Genre> genres = newFilm.getGenres();
            if (genres != null) {
                Set<Genre> setGenre = new LinkedHashSet<>(genres);
                genres = new ArrayList<>(setGenre);

                for (Genre genre : genres) {
                    if (genre != null) {
                        genreService.findGenreById(genre.getId());
                    }
                }

                newFilm.setGenres(genres);
            } else {
                newFilm.setGenres(new ArrayList<>());
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }

        String sqlQuery = "UPDATE films SET " +
                "film_name = ?, description = ?, release_Date = ?, duration = ?, rating_id = ? " +
                "where film_id = ?;";
//        UPDATE films SET  film_name = 'film',
//        description = 'description',
//        releaseDate = '1946-08-20', duration = 100, rating_id = 1 where film_id = 1;
        if (findFilmById(newFilm.getId()) == null) {
            throw new NotFoundException("Фильм с названием = " + newFilm.getName() + " не найден");
        }
        if (newFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Указанна дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        Rating mpa = newFilm.getMpa();
        Integer ratingId;
        if (mpa != null) {
            ratingId = mpa.getId();
        } else {
            ratingId = 0;
        }

        jdbcTemplate.update(sqlQuery
                , newFilm.getName()
                , newFilm.getDescription()
                , newFilm.getReleaseDate()
                , newFilm.getDuration()
                , ratingId
                , newFilm.getId());
        if (newFilm.getId() == 0) {
            throw new ValidationException("Id должен быть указан");
        }
        addGenresInFilm(newFilm.getGenres(), newFilm.getId());
        filmRowMapper.setLikesOfFilm(newFilm);
        return newFilm;
    }

    private void addGenresInFilm(List<Genre> genres, Long filmId) {
        log.info("Добавление жанра.");

        for (Genre genre : genres) {
            Integer genreId = genre.getId();
            genreService.findGenreById(genreId);

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
}