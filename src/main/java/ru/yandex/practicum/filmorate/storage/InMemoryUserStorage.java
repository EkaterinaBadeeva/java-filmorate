package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей.");
        return users.values();
    }

    public Optional<User> findUserById(Long id) {
        log.info("Получение пользователя по Id.");
        User user = users.get(id);
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(user);
        }
    }

    public User getUserById(Long id) {
        return findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
    }

    public User create(User user) {
        log.info("Добавление пользователя.");
        // проверяем выполнение необходимых условий
        checkConditions(user);

        // формируем дополнительные данные
        user.setId(getNextId());

        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        log.info("Обновление пользователя.");
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            checkConditions(newUser);

            // если пользователь найден и все условия соблюдены, обновляем информацию о нём
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }

            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с email = " + newUser.getEmail() + " не найден");
    }

    // вспомогательный метод для выполнение необходимых условий (заполнение имени пользователя и валидация даты рождения)
    private void checkConditions(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        if (user.getName().isEmpty()) {
            log.warn("Задано пустое имя пользователя");
            throw new ValidationException("Задано пустое имя пользователя");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
