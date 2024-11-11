package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTests {
    @Autowired
    FilmController filmController;

    @BeforeEach
    public void BeforeEach() {
        filmController.findAll().clear();
        Film film = new Film();
        film.setId(Long.valueOf(1));
        film.setName("Test Film");
        film.setDescription("Description of Test Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        filmController.create(film);
    }

    @Test
    public void shouldGetAllFilms() {
        //prepare
        Film film2 = new Film();
        film2.setId(Long.valueOf(2));
        film2.setName("Test Film2");
        film2.setDescription("Description of Test Film");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(100);
        filmController.create(film2);

        //do
        Collection<Film> films = filmController.findAll();

        //check
        assertEquals(2, films.size(), "Некорректное количество фильмов");
    }

    @Test
    public void shouldCreateFilm() {
        //prepare
        Film newFilm = new Film();
        newFilm.setId(Long.valueOf(2));
        newFilm.setName("Test newFilm");
        newFilm.setDescription("Description of Test newFilm");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(100);

        //do
        Film createdFilm = filmController.create(newFilm);

        //check
        assertNotNull(createdFilm.getId(), "Некоректный Id, Id нового фильма равен null");
        assertEquals(2, filmController.findAll().size(), "Некорректное количество фильмов");
        assertEquals("Test newFilm", createdFilm.getName(), "Некорректное имя фильма");
    }

    @Test
    public void shouldBeNotCreateFilmIfReleaseDateBefore25_12_1895() {
        //prepare
        Film newFilm = new Film();
        newFilm.setId(Long.valueOf(2));
        newFilm.setName("Test newFilm");
        newFilm.setDescription("Description of Test newFilm");
        newFilm.setReleaseDate(LocalDate.of(1894, 1, 1));
        newFilm.setDuration(100);

        //do
        Throwable exception = assertThrows(ValidationException.class, () -> {
            filmController.create(newFilm);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        });

        //check
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldUpdateUser() {
        //prepare
        Film film2 = new Film();
        film2.setId(Long.valueOf(2));
        film2.setName("Test Film2");
        film2.setDescription("Description of Test Film");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(100);
        filmController.create(film2);

        Film testFilm = new Film();
        testFilm.setId(film2.getId());
        testFilm.setName("Test updatedFilm");
        testFilm.setDescription("Description of Test updatedFilm");
        testFilm.setReleaseDate(LocalDate.of(2010, 2, 2));
        testFilm.setDuration(120);

        //do
        Film updatedFilm = filmController.update(testFilm);

        //check
        assertNotNull(updatedFilm.getId(), "Некоректный Id, Id обновленного фильма равен null");
        assertEquals(film2.getId(), updatedFilm.getId(), "Некоректный Id обновленного фильма");
        assertEquals(2, filmController.findAll().size(), "Некорректное количество фильмов");
        assertEquals("Test updatedFilm", updatedFilm.getName(), "Некорректное имя фильма");
    }
}
