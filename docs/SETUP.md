# Money Transfer System - Setup Guide

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.x
- Node.js LTS and Angular CLI (for frontend)
- Snowflake account (for analytics, optional)

## Backend (Spring Boot)

1. Create the database and user in MySQL:

```sql
CREATE DATABASE money_transfer_db;
CREATE USER 'money_user'@'localhost' IDENTIFIED BY 'money_pass';
GRANT ALL PRIVILEGES ON money_transfer_db.* TO 'money_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Apply schema and seed data (from the project root):

```bash
mysql -u money_user -p money_transfer_db < database/schema.sql
mysql -u money_user -p money_transfer_db < database/seed-data.sql
```

3. Start the backend:

```bash
cd backend
mvn spring-boot:run
```

APIs (secured with JWT via /api/v1/auth/login):

- `POST /api/v1/transfers`
- `GET /api/v1/accounts/{id}`
- `GET /api/v1/accounts/{id}/lookup`
- `GET /api/v1/accounts/{id}/balance`
- `GET /api/v1/accounts/{id}/transactions`
- `GET /api/v1/admin/*`

## Frontend (Angular)

Follow the steps in `frontend/README.md` to generate the Angular project and implement components/services as per the specification.

## Snowflake

Run the scripts in `snowflake/` inside your Snowflake environment to create the DW schema and example analytics queries.

