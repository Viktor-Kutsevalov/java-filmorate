package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FriendRepository;
import ru.yandex.practicum.filmorate.dal.RecommendationRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final RecommendationRepository recommendationRepository;

    @Override
    public User add(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        return userRepository.update(user);
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friendRepository.addFriend(userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        friendRepository.removeFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return friendRepository.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return friendRepository.getCommonFriends(userId, otherId);
    }

    @Override
    public List<Film> findRecommendations(Long userId, int limit) {
        return recommendationRepository.findRecommendations(userId, limit);
    }

    public Optional<User> findUserById(long id) {
        return getById(id);
    }
}