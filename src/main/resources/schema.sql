-- ============================================================
-- СХЕМА БАЗЫ ДАННЫХ FILMORATE (для H2)
-- ============================================================

DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_ratings CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- 1. ТАБЛИЦА: users (пользователи)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id     INTEGER AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    login       VARCHAR(100) NOT NULL UNIQUE,
    user_name   VARCHAR(255),
    birthday    DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. ТАБЛИЦА: mpa_ratings (рейтинги MPA)
-- ============================================================
CREATE TABLE IF NOT EXISTS mpa_ratings (
    mpa_id      INTEGER AUTO_INCREMENT PRIMARY KEY,
    mpa_name    VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ============================================================
-- 3. ТАБЛИЦА: films (фильмы)
-- ============================================================
CREATE TABLE IF NOT EXISTS films (
    film_id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    film_name       VARCHAR(255) NOT NULL,
    description     VARCHAR(200),
    release_date    DATE NOT NULL,
    duration        INTEGER CHECK (duration > 0),
    mpa_rating_id   INTEGER,
    CONSTRAINT fk_films_mpa_rating FOREIGN KEY (mpa_rating_id)
        REFERENCES mpa_ratings(mpa_id) ON DELETE SET NULL
);

-- ============================================================
-- 4. ТАБЛИЦА: genres (жанры)
-- ============================================================
CREATE TABLE IF NOT EXISTS genres (
    genre_id    INTEGER AUTO_INCREMENT PRIMARY KEY,
    genre_name  VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- 5. ТАБЛИЦА: film_genres (связь фильмов и жанров)
-- ============================================================
CREATE TABLE IF NOT EXISTS film_genres (
    film_id     INTEGER NOT NULL,
    genre_id    INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genres_film FOREIGN KEY (film_id)
        REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id)
        REFERENCES genres(genre_id) ON DELETE CASCADE
);

-- ============================================================
-- 6. ТАБЛИЦА: friendship (дружба)
-- ============================================================
CREATE TABLE IF NOT EXISTS friendship (
    user_id     INTEGER NOT NULL,
    friend_id   INTEGER NOT NULL,
    status      VARCHAR(20) DEFAULT 'UNCONFIRMED',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_friendship_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_friend FOREIGN KEY (friend_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CHECK (user_id != friend_id)
);

-- ============================================================
-- 7. ТАБЛИЦА: likes (лайки)
-- ============================================================
CREATE TABLE IF NOT EXISTS likes (
    film_id     INTEGER NOT NULL,
    user_id     INTEGER NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_likes_film FOREIGN KEY (film_id)
        REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

