# LinkBit MVP Backend 🚀

LinkBit is a decentralized lending platform that enables users to obtain INR (fiat) loans by pledging Bitcoin (BTC) as collateral. This repository contains the MVP (Minimum Viable Product) backend, providing a robust and secure foundation for trustless lending.

## 📖 Overview

LinkBit solves the liquidity problem for BTC holders without requiring them to sell their assets. By using BTC as collateral, borrowers can access INR funds provided by lenders in a peer-to-peer marketplace. The system features real-time risk management and automated LTV monitoring to protect participants.

## ✨ Core Modules & Features

### 🛡️ Authentication & KYC
-   **Secure Auth**: JWT-based authentication for all API endpoints.
-   **KYC System**: Integrated workflow for user verification (Pending/Verified/Rejected) to ensure regulatory compliance.
-   **Security**: Rate-limiting on sensitive endpoints and encrypted credential storage.

### 🏪 Loan Marketplace
-   **Offer Creation**: Lenders can publish loan offers with custom interest rates, LTV requirements, and tenures.
-   **Discovery**: Borrowers can filter and discover offers that match their financial needs.
-   **Seamless Connection**: One-click initiation of loan negotiations from the marketplace.

### 🤝 Negotiation & Term Sheets
-   **Dynamic Negotiation**: Real-time updates to loan terms during the negotiation phase.
-   **Smart Term Sheets**: Generation of immutable agreement hashes (SHA256) once terms are finalized.
-   **Digital Signatures**: Multi-signature flow requiring both parties to sign the finalized contract.

### 🏛️ Escrow & Collateral
-   **Mock BTC Escrow**: Simulated Bitcoin address generation and transaction tracking for collateral management.
-   **Collateral Release**: Automated or manual release of BTC upon successful loan repayment.

### 📉 LTV Risk Management
-   **Real-time Monitoring**: Background workers (Heartbeat) poll BTC/INR prices via CoinGecko to recalculate LTV.
-   **Automated Alerts**: Transitions loans to `MARGIN_CALL` or `LIQUIDATION_ELIGIBLE` based on price volatility.
-   **Admin Tools**: Manual risk state overrides for system administrators.

### 💸 Payments & Ledger
-   **Disbursement Tracking**: Managed workflow for fiat fund transfer verification.
-   **EMI & Repayments**: Flexible repayment scheduling (EMI or Bullet) with receipt verification.
-   **System Ledger**: A transparent, immutable log of all financial transitions within the platform.

## 🛠 Technology Stack

-   **Backend**: Java 21, Spring Boot 3.2.3
-   **Security**: Spring Security, io.jsonwebtoken (JWT)
-   **Persistence**: PostgreSQL (Prod/Staging), H2 (Local/Test)
-   **Migrations**: Flyway
-   **Communication**: WebSockets (STOMP) for negotiation chat
-   **Price Oracle**: CoinGecko API Integration
-   **Documentation**: SpringDoc OpenAPI (Swagger UI)

## 📁 Project Structure

```text
src/main/java/com/linkbit/mvp/
├── config/        # Security, WebSocket, and App configurations
├── controller/    # REST API endpoints (Web layer)
├── domain/        # JPA Entities, Enums, and Business Models
├── dto/           # Data Transfer Objects for API requests/responses
├── exception/     # Centralized exception handling logic
├── repository/    # Spring Data JPA interfaces for DB access
└── service/       # Core business logic, workers, and integrations
```

## 🚥 Getting Started

### Prerequisites
-   Java 21 JDK
-   Maven 3.x
-   PostgreSQL (optional, defaults to H2 for local development)

### Running Locally
1.  **Clone the Repo**: `git clone <repo-url>`
2.  **Environment**: Ensure your `application.yml` or `application-local.yml` is configured.
3.  **Launch**:
    ```bash
    ./mvnw spring-boot:run
    ```
The server will be available at `http://localhost:8080`.

### API Exploration
Access the interactive Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

## 🧪 Testing

LinkBit maintains a high standard of code quality through a comprehensive test suite.

-   **Test Coverage**: Includes Controllers, Services, and background LTV workers.
-   **Details**: For a granular breakdown of individual test cases, see [TEST_CASES.md](./TEST_CASES.md).
-   **Execution**:
    ```bash
    ./mvnw test
    ```

## 📄 License

This project is proprietary and confidential. Unauthorized copying or distribution is prohibited.
