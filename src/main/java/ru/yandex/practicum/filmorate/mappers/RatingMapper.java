package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.model.Rating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RatingMapper {
    public static RatingDto mapToRatingDto(Rating rating) {
        RatingDto ratingDto = RatingDto.builder()
                .id(rating.getId())
                .name(rating.getName())
                .build();
        return ratingDto;
    }

    public static Rating mapToRating(RatingDto ratingDto) {
        Rating rating = Rating.builder()
                .id(ratingDto.getId())
                .name(ratingDto.getName())
                .build();
        return rating;
    }
}
