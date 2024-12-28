package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserRowMapper implements RowMapper<User> {
    private final JdbcTemplate jdbcTemplate;

    public UserRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .id(rs.getLong(1))
                .email(rs.getString(2))
                .login(rs.getString(3))
                .name(rs.getString(4))
                .birthday(rs.getDate(5).toLocalDate())
                .build();
        setFriendsOfUser(user);
        return user;
    }

    public void setFriendsOfUser(User user) {
        String sqlQueryFriends = "SELECT friend_id FROM FRIENDSHIP where user_id = ?;";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQueryFriends, user.getId());
        user.setFriends(rows.stream()
                .map(key -> ((Number) key.get("friend_id")).longValue())
                .collect(Collectors.toSet())
        );
    }
}
