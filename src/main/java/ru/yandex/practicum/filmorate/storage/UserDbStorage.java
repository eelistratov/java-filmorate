package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("user_name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            loadFriends(user);
            return user;
        }
    };

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY user_id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (email, login, user_name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateFriends(user);
        log.debug("Пользователь добавлен с id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        updateFriends(user);
        log.debug("Пользователь с id {} обновлён", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        log.debug("Пользователь с id {} удалён", id);
    }

    @Override
    public boolean userExists(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Integer> friendIds = jdbcTemplate.queryForList(sql, Integer.class, user.getId());
        friendIds.forEach(user::addFriend);
    }

    private void updateFriends(User user) {
        String deleteSql = "DELETE FROM friendship WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
            for (Integer friendId : user.getFriends()) {
                jdbcTemplate.update(sql, user.getId(), friendId);
            }
        }
    }
}