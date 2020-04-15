CREATE TABLE users
(
    id           TEXT        NOT NULL PRIMARY KEY,
    email        TEXT        NOT NULL,
    country_code TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL
);


CREATE TABLE addresses
(
    user_id TEXT NOT NULL REFERENCES users (id),
    street  TEXT NOT NULL
);


CREATE TABLE person
(
    id   SERIAL NOT NULL PRIMARY KEY,
    name TEXT   NOT NULL,
    age  INT    NULL
);

CREATE TABLE points
(
    x INT NOT NULL,
    y INT NOT NULL
);
