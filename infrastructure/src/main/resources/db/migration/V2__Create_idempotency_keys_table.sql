CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) NOT NULL,
    response_body TEXT NOT NULL,
    response_status INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (idempotency_key)
);