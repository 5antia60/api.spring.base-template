CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       name       VARCHAR(100)        NOT NULL,
                       email      VARCHAR(150)        NOT NULL UNIQUE,
                       password   VARCHAR(255)        NOT NULL,
                       role       VARCHAR(20)         NOT NULL DEFAULT 'USER',
                       "createdAt" TIMESTAMP          NOT NULL DEFAULT NOW(),
                       "updatedAt" TIMESTAMP
);