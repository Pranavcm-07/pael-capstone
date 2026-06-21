-- Snowflake data warehouse objects for Money Transfer System

CREATE OR REPLACE DATABASE MONEY_TRANSFER_DW;
CREATE OR REPLACE SCHEMA MONEY_TRANSFER_DW.ANALYTICS;

USE DATABASE MONEY_TRANSFER_DW;
USE SCHEMA ANALYTICS;

CREATE OR REPLACE TABLE DIM_ACCOUNT (
    account_key NUMBER AUTOINCREMENT,
    account_id NUMBER,
    holder_name STRING,
    status STRING,
    effective_date DATE,
    PRIMARY KEY (account_key)
);

CREATE OR REPLACE TABLE DIM_DATE (
    date_key NUMBER,
    full_date DATE,
    day NUMBER,
    month NUMBER,
    year NUMBER,
    quarter NUMBER,
    PRIMARY KEY (date_key)
);

CREATE OR REPLACE TABLE FACT_TRANSACTIONS (
    transaction_key NUMBER AUTOINCREMENT,
    account_from_key NUMBER,
    account_to_key NUMBER,
    date_key NUMBER,
    amount NUMBER(18,2),
    status STRING,
    PRIMARY KEY (transaction_key)
);

