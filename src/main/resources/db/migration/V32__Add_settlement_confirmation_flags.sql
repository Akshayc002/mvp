-- V32: Add mutual settlement confirmation flags to loans
ALTER TABLE loans ADD COLUMN borrower_settlement_confirmed BOOLEAN DEFAULT FALSE;
ALTER TABLE loans ADD COLUMN lender_settlement_confirmed BOOLEAN DEFAULT FALSE;
