-- Stage for loading transaction data (adjust URL/credentials as needed)
CREATE OR REPLACE STAGE TRANSACTION_STAGE;

-- Example COPY INTO from staged CSV into FACT_TRANSACTIONS via a temporary table
-- (Assumes date_key, account keys are pre-populated)

-- COPY INTO FACT_TRANSACTIONS
-- FROM @TRANSACTION_STAGE/transactions.csv
-- FILE_FORMAT = (TYPE = 'CSV' SKIP_HEADER = 1);

-- Analytics queries

-- Daily Transaction Volume
SELECT d.full_date,
       COUNT(*) AS txn_count,
       SUM(f.amount) AS total_amount
FROM FACT_TRANSACTIONS f
JOIN DIM_DATE d ON f.date_key = d.date_key
GROUP BY d.full_date
ORDER BY d.full_date;

-- Most Active Accounts
SELECT a.account_id,
       a.holder_name,
       COUNT(*) AS txn_count,
       SUM(f.amount) AS total_amount
FROM FACT_TRANSACTIONS f
JOIN DIM_ACCOUNT a ON f.account_from_key = a.account_key
GROUP BY a.account_id, a.holder_name
ORDER BY txn_count DESC
LIMIT 10;

-- Success Rate
SELECT status,
       COUNT(*) AS txn_count,
       ROUND(100 * COUNT(*) / SUM(COUNT(*)) OVER (), 2) AS pct
FROM FACT_TRANSACTIONS
GROUP BY status;

-- Peak Hours (assuming DIM_DATE has hour; if not, derive from timestamp in staging)
-- SELECT d.hour, COUNT(*) AS txn_count
-- FROM FACT_TRANSACTIONS f
-- JOIN DIM_DATE d ON f.date_key = d.date_key
-- GROUP BY d.hour
-- ORDER BY txn_count DESC;

