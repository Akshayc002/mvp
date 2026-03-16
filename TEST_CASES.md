# LinkBit MVP Test Suite Details

This document provides a detailed overview of the test cases implemented in the LinkBit MVP backend to ensure system reliability and security.

## 🔑 Authentication & Security
**Files**: `AuthControllerTest.java`

- **Successful Registration**: Verifies that new users can register with valid details, passwords are encrypted, and KYC status is set to PENDING.
- **Login Flow**: Validates successful login and profile retrieval (`/auth/me`) using JWT tokens.
- **Security & Rate Limiting**: Ensures that repeated failed login attempts trigger a `429 Too Many Requests` response to prevent brute-force attacks.
- **Password Reset**: Tests the forgot/reset password flow, ensuring tokens are handled securely and the new password works correctly.

## 🏪 Loan Marketplace
**Files**: `LoanMarketplaceControllerTest.java`

- **Offer Creation**: Verifies that verified lenders can create loan offers with specific terms.
- **KYC Enforcement**: Ensures that unverified users (KYC PENDING) cannot create loan offers.
- **Offer Discovery**: Tests the ability for borrowers to browse and filter open loan offers based on amount.
- **Connection Logic**: Validates the process of a borrower connecting to an available loan offer to initiate a loan.

## 🤝 Negotiation & Contracts
**Files**: `NegotiationControllerTest.java`

- **Term Updates**: Tests the ability for parties to update loan terms (principal, interest, tenure, LTV thresholds) during the negotiation phase.
- **Contract Finalization**: Verifies that finalizing a negotiation generates a unique agreement hash (SHA256) and closes the original offer.
- **Digital Signatures**: Validates the multi-party signing process, ensuring signatures are correctly recorded for both lender and borrower.
- **Cancellation**: Ensures that negotiations can be cancelled by either party before finalization.

## 📉 LTV Risk Management (Heartbeat)
**Files**: `LtvMonitoringWorkerTest.java`, `AdminRiskControllerTest.java`

- **LTV Monitoring**: Tests the background worker's ability to calculate real-time LTV using mock BTC prices from `BtcPriceService`.
- **Margin Call Transition**: Verifies that the system automatically moves a loan to `MARGIN_CALL` status when the LTV exceeds the defined threshold.
- **Liquidation Eligibility**: Validates the transition to `LIQUIDATION_ELIGIBLE` when LTV breaches the critical threshold.
- **Recovery Logic**: Ensures loans return to `ACTIVE` status if the BTC price recovers and LTV falls back below the margin threshold.
- **Admin Overrides**: Tests the admin's ability to manually override a loan's risk state for emergency management.

## 💸 Payments & Repayments
**Files**: `PaymentControllerTest.java`, `RepaymentControllerTest.java`

- **Disbursement Flow**: Validates that lenders can confirm disbursement and borrowers can acknowledge receipt.
- **Repayment Tracking**: Tests the creation and payment of EMI installments, including payment proof verification.
- **Ledger Integration**: Ensures that every payment event is correctly recorded in the system ledger for transparency.

## 🛠 Running the Tests

To execute the full test suite, run the following command from the project root:

```bash
./mvnw test
```

Tests use an in-memory H2 database and a "test" Spring profile to ensure isolation from development or production environments.
