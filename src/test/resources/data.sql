MERGE INTO mpa_ratings (id, name) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

MERGE INTO genres (id, name) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

MERGE INTO users (id, email, login, name, birthday) KEY(id)
VALUES (1, 'test@example.com', 'testLogin', 'Test User', '2000-01-01');
MERGE INTO users (id, email, login, name, birthday) KEY(id)
VALUES (2, 'testRev@yandex.ru', 'Login2', 'User2', '1999-09-09');
MERGE INTO users (id, email, login, name, birthday) KEY(id)
VALUES (3, 'testCommon@yandex.ru', 'Login3', 'User3', '2000-10-10');
MERGE INTO films (id, name, description, release_date, duration, mpa_id) KEY(id)
VALUES (1, 'Film1', 'Description1', '2020-01-01', 120, 1);
MERGE INTO films (id, name, description, release_date, duration, mpa_id) KEY(id)
VALUES (2, 'Film2', 'Description2', '2000-01-01', 113, 1);