CREATE TABLE IF NOT EXISTS ACCOUNTS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holder_name VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP(6) NULL
);

CREATE TABLE IF NOT EXISTS TRANSACTION_LOGS (
    id CHAR(36) PRIMARY KEY,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500) NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    created_on TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key),
    INDEX idx_from_account_id (from_account_id),
    INDEX idx_to_account_id (to_account_id)
);
