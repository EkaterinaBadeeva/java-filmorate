package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryFilmService implements FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final RatingService ratingService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<FilmDto> findAllFilms() {
        log.info("Получение всех фильмов.");
        return filmStorage.getAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    @Override
    public FilmDto findFilmById(Long id) {
        log.info("Получение фильма по id.");
        return filmStorage.getFilmById(id)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
    }

    @Override
    public FilmDto create(FilmDto filmDto) {
        log.info("Добавление фильма.");
        Film film = FilmMapper.mapToFilm(filmDto);
        checkConditions(film);
        film = filmStorage.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    @Override
    public FilmDto update(FilmDto filmDto) {
        Film film = FilmMapper.mapToFilm(filmDto);
        checkConditions(film);
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с названием = " + film.getName() + " не найден");
        }
        film = filmStorage.update(film);
        return FilmMapper.mapToFilmDto(film);
    }

    //PUT /films/{id}/like/{userId}
    // пользователь ставит лайк фильму
    public FilmDto addUserLike(Long id, Long userId) {
        log.info("Добавление лайка фильму от пользователя.");

        // проверяем необходимые условия
        checkId(id);
        checkId(userId);

        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
        userService.findUserById(userId);

        // если пользователь и фильм найдены и все условия соблюдены, добавляем лайк от пользователя
        film = filmStorage.addUserLike(id, userId);
        return FilmMapper.mapToFilmDto(film);
    }

    //DELETE /films/{id}/like/{userId}
    // пользователь удаляет лайк
    public FilmDto deleteUserLike(Long id, Long userId) {
        log.info("Удаление лайка фильму от пользователя.");

        // проверяем необходимые условия
        checkId(id);
        checkId(userId);

        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + id + " не найден"));
        userService.findUserById(userId);

        // если пользователь и фильм найдены и все условия соблюдены, удаляем лайк от пользователя
        filmStorage.deleteUserLike(id, userId);

        return FilmMapper.mapToFilmDto(film);
    }

    //GET /films/popular?count={count}
    // возвращает список из первых count фильмов по количеству лайков
    // Если значение параметра count не задано, возвращает первые 10
    public List<FilmDto> findBestFilm(Long count) {

        return filmStorage.getAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .sorted(Comparator.comparing((film) -> film.getLikes().size()))
                .limit(count)
                .toList()
                .reversed();
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
}
