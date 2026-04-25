-- V31: Final cleanup of all non-admin data
-- Admin ID: '123e4567-e89b-12d3-a456-426614174001'

-- Clear all transaction and loan data
DELETE FROM platform_fees;
DELETE FROM loan_ledger;
DELETE FROM loan_repayments;
DELETE FROM loan_emis;
DELETE FROM loan_audit_log;
DELETE FROM loan_margin_calls;
DELETE FROM loan_ltv_history;
DELETE FROM bitcoin_transactions;
DELETE FROM escrow_accounts;
DELETE FROM loan_liquidations;
DELETE FROM collateral_releases;
DELETE FROM negotiation_messages;
DELETE FROM loan_offers;
DELETE FROM loans;
DELETE FROM notifications;
DELETE FROM password_reset_token;

-- Clear user related tables
DELETE FROM user_kyc_details WHERE user_id != '123e4567-e89b-12d3-a456-426614174001';

-- Finally clear users except admin
DELETE FROM users WHERE id != '123e4567-e89b-12d3-a456-426614174001';
