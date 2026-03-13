# LinkBit MVP Backend

LinkBit is a decentralized lending platform that enables users to obtain INR (fiat) loans by pledging Bitcoin (BTC) as collateral. This repository contains the MVP (Minimum Viable Product) backend, built with Spring Boot.

## 🚀 Overview

The LinkBit platform facilitates trustless lending between borrowers and lenders. Borrowers can lock their BTC in a simulated escrow, and lenders provide fiat funds. The system monitors the Loan-to-Value (LTV) ratio in real-time to manage risk.

## ✨ Key Features

-   **User Authentication & KYC**: Secure registration and login using JWT. KYC status tracking (Pending/Verified).
-   **Loan Offer Marketplace**: Lenders can create loan offers; borrowers can browse and filter offers based on amount, tenure, and interest rates.
-   **Negotiation System**: Real-time WebSocket-based chat for negotiating loan terms (LTV, Interest, Tenure).
-   **Smart Term Sheets**: Immutable contract formation with SHA256 hashing.
-   **Digital Signatures**: Multi-party signing process for loan agreements.
-   **Bitcoin Escrow (Mock)**: Simulated Bitcoin escrow system with address generation and transaction tracking.
-   **Real-time LTV Monitoring**: Background worker that polls BTC/INR prices via CoinGecko and triggers Margin Calls or Liquidation if LTV thresholds are breached.
-   **Fiat Disbursement Tracking**: Workflow for lenders to mark funds as disbursed and borrowers to confirm receipt.
-   **Repayment & Ledger**: EMI calculation, repayment tracking with payment proof verification, and a transparent system ledger.

## 🛠 Technology Stack

-   **Language**: Java 21
-   **Framework**: Spring Boot 3.2.3
-   **Security**: Spring Security, JWT (io.jsonwebtoken)
-   **Database**: PostgreSQL (Production/Staging), H2 (Local Development/Test)
-   **Database Migrations**: Flyway
-   **Communication**: WebSockets (STOMP)
-   **Documentation**: SpringDoc OpenAPI (Swagger UI)
-   **External APIs**: CoinGecko (Price Oracle)

## 📁 Project Structure

```text
src/main/java/com/linkbit/mvp/
├── config/        # Security, WebSocket, and App configurations
├── controller/    # REST API endpoints
├── domain/        # JPA Entities and Enums
├── dto/           # Data Transfer Objects
├── exception/     # Global Exception Handling
├── repository/    # Spring Data JPA Repositories
└── service/       # Business Logic and Background Workers
```

## 🚥 Getting Started

### Prerequisites

-   Java 21 JDK
-   Maven 3.x
-   PostgreSQL (Optional, H2 used by default for local dev)

### Database Setup

The project uses Flyway for migrations. On the first run, it will automatically create the necessary tables in the H2/Postgres database.

### Running Locally

1.  Clone the repository.
2.  Navigate to the project root.
3.  Run the application using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```

The server will start on `http://localhost:8080`.

## 📖 API Documentation

Once the application is running, you can access the Swagger UI documentation at:
`http://localhost:8080/swagger-ui/index.html`

## 🧪 Testing

The project includes a comprehensive test suite covering controllers and background workers.

To run all tests:
```bash
./mvnw test
```

## 📄 License

This project is proprietary.
