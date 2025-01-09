package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenreMapper {
    public static GenreDto mapToGenreDto(Genre genre) {
        GenreDto genreDto = GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
        return genreDto;
    }

    public static Genre mapToGenre(GenreDto genreDto) {
        Genre genre = Genre.builder()
                .id(genreDto.getId())
                .name(genreDto.getName())
                .build();
        return genre;
    }
}
