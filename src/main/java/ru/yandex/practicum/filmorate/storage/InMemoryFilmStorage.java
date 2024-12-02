package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        log.info("Получение всех фильмов.");
        return films.values();
    }

    public Film findFilmById(Long id) {
        log.info("Получение фильма по id.");
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с Id " + id + " не найден");
        }
        return film;
    }

    public Film create(Film film) {
        log.info("Добавление фильма.");
        // проверяем выполнение необходимых условий

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Указанна дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        // формируем дополнительные данные
        film.setId(getNextId());

        // сохраняем нового пользователя в памяти приложения
        films.put(film.getId(), film);
        return film;
    }

    public Film update(Film newFilm) {
        log.info("Обновление фильма.");
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.warn("Указанна дата релиза раньше 28 декабря 1895 года");
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
            }

            // если фильм найден и все условия соблюдены, обновляем информацию о нём
            if (newFilm.getName() != null) {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null) {
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null) {
                oldFilm.setDuration(newFilm.getDuration());
            }

            return oldFilm;
        }
        throw new NotFoundException("Фильм с названием = " + newFilm.getName() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
