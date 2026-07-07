CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    identity_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100),

    created_at TIMESTAMP,
    updated_at TIMESTAMP
);