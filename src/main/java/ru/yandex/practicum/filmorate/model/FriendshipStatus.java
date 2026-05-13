package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    PENDING, // неподтверждённая — когда один пользователь отправил запрос на добавление другого пользователя в друзья
    CONFIRMED // подтверждённая — когда второй пользователь согласился на добавление.
}