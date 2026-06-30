package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    User update(User user);

    Optional<User> getById(Long id);

    List<User> getAll();

    void deleteById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);

    List<Film> findRecommendations(Long userId, int limit);
}
