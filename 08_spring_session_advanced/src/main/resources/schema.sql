-- 기존 테이블/타입 제거
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS persistent_logins CASCADE;
DROP TYPE IF EXISTS role_type;

-- Role enum 매핑용 타입
CREATE TYPE role_type AS ENUM ('ADMIN', 'USER');

-- users 테이블
CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE          DEFAULT now(),

    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(60)  NOT NULL,
    role        role_type    NOT NULL DEFAULT 'USER'
);

-- posts 테이블
CREATE TABLE posts
(
    id          BIGSERIAL PRIMARY KEY,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE          DEFAULT now(),

    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT false,

    author_id   BIGINT       NOT NULL,
    CONSTRAINT fk_post_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);


-- Remember Me 테이블
CREATE TABLE IF NOT EXISTS persistent_logins (
     username  varchar(64) not null,
     series    varchar(64) primary key,
     token     varchar(64) not null,
     last_used timestamp   not null
);
