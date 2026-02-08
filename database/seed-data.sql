INSERT INTO ACCOUNTS (holder_name, balance, status, version, last_updated)
SELECT 'Alice Smith', 5000.00, 'ACTIVE', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ACCOUNTS LIMIT 1);

INSERT INTO ACCOUNTS (holder_name, balance, status, version, last_updated)
SELECT 'Bob Jones', 3500.50, 'ACTIVE', 0, CURRENT_TIMESTAMP
WHERE (SELECT COUNT(*) FROM ACCOUNTS) < 2;

INSERT INTO ACCOUNTS (holder_name, balance, status, version, last_updated)
SELECT 'Carol White', 10000.00, 'ACTIVE', 0, CURRENT_TIMESTAMP
WHERE (SELECT COUNT(*) FROM ACCOUNTS) < 3;
