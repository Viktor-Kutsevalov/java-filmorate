package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.debug("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.debug("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserOrThrow(userId);
        return user.getFriends().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}
