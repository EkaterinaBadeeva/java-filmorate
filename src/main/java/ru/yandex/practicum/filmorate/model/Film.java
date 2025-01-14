package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class Film {

    Long id;

    @NotNull
    @NotBlank
    String name;

    @Size(max = 200)
    String description;

    @NotNull
    LocalDate releaseDate;

    @NotNull
    @Positive
    Integer duration;

    Set<Long> likes = new HashSet<>();

    List<Genre> genres = new ArrayList<>();

    Rating mpa;
}
