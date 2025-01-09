package ru.yandex.practicum.filmorate.storage.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = Film.builder()
                .id(rs.getLong(1))
                .name(rs.getString(2))
                .description(rs.getString(3))
                .releaseDate(rs.getDate(4).toLocalDate())
                .duration(rs.getInt(5))
                .build();

        setLikesOfFilm(film);

        return film;
    }

    public void setLikesOfFilm(Film film) {
        String sqlQueryLikes = "SELECT user_id FROM LIKES where film_id = ?;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQueryLikes, film.getId());
        film.setLikes(rows.stream()
                .map(key -> ((Number) key.get("user_id")).longValue())
                .collect(Collectors.toSet())
        );
    }
}
