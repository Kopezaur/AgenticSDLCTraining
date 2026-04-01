CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date    DATE,
    CONSTRAINT chk_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'))
);
