CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    balance DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    from_account BIGINT,
    to_account BIGINT,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    idempotency_key VARCHAR(100) UNIQUE,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_from_account FOREIGN KEY (from_account) REFERENCES accounts(id),
    CONSTRAINT fk_to_account FOREIGN KEY (to_account) REFERENCES accounts(id)
);

CREATE TABLE reward_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_id VARCHAR(36) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL,
    points_earned INT NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reward_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reward_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_reward_transaction FOREIGN KEY (transaction_id) REFERENCES transaction_logs(id)
);

