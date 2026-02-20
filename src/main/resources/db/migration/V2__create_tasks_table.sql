CREATE TABLE tasks (
                       id          BIGSERIAL PRIMARY KEY,
                       title       VARCHAR(200)   NOT NULL,
                       description TEXT,
                       status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
                       "userId"    BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       "createdAt" TIMESTAMP      NOT NULL DEFAULT NOW(),
                       "updatedAt" TIMESTAMP
);