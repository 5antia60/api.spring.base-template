CREATE TABLE tasks (
                       id          BIGSERIAL PRIMARY KEY,
                       title       VARCHAR(200)   NOT NULL,
                       description TEXT,
                       status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
                       user_id    BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP WITH TIME ZONE
);