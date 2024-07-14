CREATE SCHEMA authentication
    AUTHORIZATION pg_database_owner;

CREATE TABLE authentication."user"
(
    user_id serial NOT NULL,
    name text NOT NULL,
    login text NOT NULL,
    password_salted_hash text NOT NULL,
    password_salt text NOT NULL,
    role text NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE (login),
    UNIQUE (name)
);

ALTER TABLE IF EXISTS authentication."user"
    OWNER to pg_database_owner;

INSERT INTO authentication."user" VALUES (1, 'root', 'dOEWvx9pNtXQuwKbq9NH2tmvU6Y5q+pH4THym026q6U=', 'n\poeDfqvujZkBfT', 'superuser', 'ADMIN');
INSERT INTO authentication."user" VALUES (2, 'covertops69', 'FsdXFovasGCPlLJeynBXzJZ6vUF8OaAIn0Kjgwj6mPg=', 'FELvSxAippT]Ejkx', 'Alice', 'USER');
INSERT INTO authentication."user" VALUES (3, 'bob1955', 'Sy/0z2ReF7S4tWZ9QquRO55/Ttyf/ZG202IYUr5Vd/A=', 'Z\asIdgkbANLQKVs', 'Bob', 'USER');

SELECT pg_catalog.setval('authentication.user_user_id_seq', 3, true);