# Money Transfer System 💸

A simple and secure web application to transfer money between bank accounts. This project was built to demonstrate a full-stack microservice architecture.

## 🚀 Features

- **User Authentication**: Secure login system.
- **Money Transfer**: Send money instantly to other accounts.
- **Transaction History**: View a complete log of all sent and received money.
- **Smart Validations**: Prevents errors like insufficient funds or transferring to the same account.
- **Real-time Feedback**: Instant success or error messages.

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot, Spring Data JPA
- **Frontend**: Angular 17, TypeScript, CSS
- **Database**: MySQL
- **Build Tools**: Maven (Backend), Angular CLI (Frontend)

## 🏃‍♂️ How to Run

### 1. Database Setup
Ensure MySQL is running and creates a database named `money_transfer_db`. The app will automatically create tables and seed demo data.

### 2. Start the Backend
Navigate to the `backend` folder and run:
`mvn spring-boot:run`

The server will start on `http://localhost:8080`.

### 3. Start the Frontend
Navigate to the `frontend/money-transfer-app` folder and run:
`ng serve -o`

The application will open at `http://localhost:4200`.

## 🧪 Demo Credentials

Use these accounts to test the application:

| User | Username | Password | Account ID |
|------|----------|----------|------------|
| **Pranav** | `Pranav` | `pranav123` | `1` |
| **Pranesh** | `Pranesh` | `pranesh123` | `2` |

Try logging in as **Pranav** and transferring money to **Pranesh** (Account ID `2`)!

## 📝 API Endpoints

- `POST /api/auth/login` - User login
- `POST /api/v1/transfers` - Initiate a transfer
- `GET /api/v1/accounts/{id}/balance` - Check balance
- `GET /api/v1/accounts/{id}/transactions` - Get transaction history

---
*Created for Capstone Project Presentation*
