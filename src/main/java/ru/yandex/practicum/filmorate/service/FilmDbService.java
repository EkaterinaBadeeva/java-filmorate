package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.CommonException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class FilmDbService implements FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final RatingService ratingService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Collection<FilmDto> findAllFilms() {
        log.info("Получение всех фильмов.");
        return filmStorage.getAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .peek(this::addGenreAndMpa)
                .collect(Collectors.toList());
    }

    public FilmDto findFilmById(Long id) {
        log.info("Получение фильма по id.");
        return filmStorage.getFilmById(id)
                .map(FilmMapper::mapToFilmDto)
                .map(film -> {
                    addGenreAndMpa(film);
                    return film;
                })
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
    }

    public FilmDto create(FilmDto filmDto) {
        log.info("Добавление фильма.");
        Film film = FilmMapper.mapToFilm(filmDto);
        checkConditions(film);
        film = filmStorage.create(film);

        FilmDto newFilmDto = FilmMapper.mapToFilmDto(film);
        addGenreAndMpa(newFilmDto);

        return newFilmDto;
    }

    public FilmDto update(FilmDto filmDto) {
        log.info("Обновление фильма.");
        Film film = FilmMapper.mapToFilm(filmDto);
        checkConditions(film);
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с названием = " + film.getName() + " не найден");
        }
        film = filmStorage.update(film);

        FilmDto newFilmDto = FilmMapper.mapToFilmDto(film);
        addGenreAndMpa(newFilmDto);

        return newFilmDto;
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    @Override
    public FilmDto addUserLike(Long id, Long userId) {
        log.info("Добавление лайка фильму от пользователя.");

        checkId(id);
        checkId(userId);

        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
        userService.findUserById(userId);

        if (film.getLikes().contains(userId)) {
            throw new CommonException("Вы уже поставили лайк этому фильму. Мы рады, что он вам так понравился");
        }

        film = filmStorage.addUserLike(id, userId);
        return FilmMapper.mapToFilmDto(film);
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    @Override
    public FilmDto deleteUserLike(Long id, Long userId) {
        log.info("Удаление лайка фильму от пользователя.");

        checkId(id);
        checkId(userId);

        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
        userService.findUserById(userId);

        if (film.getLikes().contains(userId)) {
            filmStorage.deleteUserLike(id, userId);
        }

        return filmStorage.getFilmById(film.getId())
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возвращает первые 10
    @Override
    public List<FilmDto> findBestFilm(Long count) {
        log.info(MessageFormat.format("Возвращает список из первых {0} фильмов по количеству лайков", count));
        return filmStorage.findBestFilm(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .peek(this::addGenreAndMpa)
                .collect(Collectors.toList());
    }

    private void checkId(Long id) {
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
    }

    private void checkConditions(Film film) {
        try {
            Rating mpa = film.getMpa();
            if (mpa != null) {
                ratingService.findRatingById(mpa.getId());
            }

            List<Genre> genres = film.getGenres();
            if (genres != null) {
                Set<Genre> setGenre = new LinkedHashSet<>(genres);
                genres = new ArrayList<>(setGenre);

                for (Genre genre : genres) {
                    if (genre != null) {
                        genreService.findGenreById(genre.getId());
                    }
                }

                film.setGenres(genres);
            } else {
                film.setGenres(new ArrayList<>());
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Указанна дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void addGenreAndMpa(FilmDto filmDto) {
        try {
            Long filmId = filmDto.getId();
            Rating mpa = ratingService.getRatingOfFilm(filmId);
            if (mpa != null) {
                filmDto.setMpa(mpa);
            }

            filmDto.setGenres(genreService.getGenresOfFilm(filmId));
        } catch (NotFoundException e) {
            throw new ValidationException(e.getMessage());
        }
    }
}
