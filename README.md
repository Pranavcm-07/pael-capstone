# Money Transfer System

A full-stack money transfer application implementing a digital **Money Transfer System** with Spring Boot backend and Angular frontend.

## Modules

- GIT: Repository structure and branching strategy
- Advanced Java: Domain models, DTOs, exceptions, and unit tests
- Spring Boot: REST APIs, services, security, database access, and AOP logging
- Angular: Single Page Application frontend
- Snowflake: Analytics data warehouse and queries

## Project Structure

```text
money-transfer-system/
  backend/        # Spring Boot application (Java 17)
  frontend/       # Angular SPA
  database/       # MySQL DDL and seed data
  snowflake/      # Snowflake DDL and analytics queries
  docs/           # Additional documentation
```

## Prerequisites

Ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 9.7 LTS** (or MySQL 8.x)
- **Node.js LTS** (v22.14.0+)
- **Angular CLI** (v21.2.15+)

Verify installations:

```bash
java --version
mvn -v
mysql --version
node -v
ng version
```

## Database Setup

### 1. Create Database

```bash
mysql -u root -p -e "CREATE DATABASE money_transfer;"
```

### 2. Import Schema and Seed Data

```bash
mysql -u root -p money_transfer < database/schema.sql
mysql -u root -p money_transfer < database/seed-data.sql
```

### 3. Update Backend Configuration

Update `backend/src/main/resources/application.yml` with your MySQL root password:

```yaml
spring:
  datasource:
    password: YOUR_MYSQL_ROOT_PASSWORD
```

**Test Users (password: `password`):**

- alice - $10,000 balance
- bob - $5,000 balance
- charlie - $2,500 balance

## Running the Application

### Start Backend Server

Open a Command Prompt and run:

```bash
cd C:\Capstone\money-transfer-system\backend
"C:\Program Files\maven\apache-maven-3.9.16\bin\mvn" spring-boot:run
```

Backend will start on **http://localhost:8080**

### Start Frontend Server

Open a new Command Prompt and run:

```bash
cd C:\Capstone\money-transfer-system\frontend
ng serve
```

Frontend will start on **http://localhost:4200**

## Accessing the Application

1. Open your browser and go to **http://localhost:4200**
2. Login with test credentials (e.g., username: `alice`, password: `password`)
3. You can now:
   - View account balance
   - View transaction history
   - Transfer money to other accounts
   - View analytics dashboard

## API Endpoints

All endpoints require JWT authentication.

- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `POST /api/v1/transfers` - Create money transfer
- `GET /api/v1/accounts/{id}` - Get account details
- `GET /api/v1/accounts/{id}/balance` - Get account balance
- `GET /api/v1/accounts/{id}/transactions` - Get transaction history

## Troubleshooting

### Backend won't start - "Access denied for user 'root'"

- Check your MySQL password in `application.yml`
- Verify MySQL is running with the correct credentials

### Frontend won't start - "ng: not recognized"

- Reinstall Angular CLI: `npm install -g @angular/cli`

### Can't connect to database

- Ensure MySQL service is running
- Verify database exists: `mysql -u root -p money_transfer -e "SHOW TABLES;"`

### Port already in use

- Backend (8080): Change `server.port` in `application.yml`
- Frontend (4200): Run `ng serve --port 4300`

## Technology Stack

- **Backend**: Spring Boot 3.3.0, Spring Data JPA, MySQL Connector
- **Frontend**: Angular 21.1.3, TypeScript, Bootstrap
- **Database**: MySQL 9.7 LTS
- **Build**: Maven, npm
- **Authentication**: JWT

## Development

### Build Backend

```bash
cd backend
mvn clean package
```

### Build Frontend

```bash
cd frontend
npm run build
```

## Notes

- Both backend and frontend servers must be running simultaneously
- Frontend communicates with backend via REST API on port 8080
- Authentication is handled via JWT tokens
- All transactions are logged in the database
