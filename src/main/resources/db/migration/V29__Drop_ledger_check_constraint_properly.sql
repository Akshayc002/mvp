-- Recreate loan_ledger table to remove the restrictive check constraint from V7
-- This allows all values from the LedgerEntryType enum.

CREATE TABLE loan_ledger_temp (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount_inr DECIMAL NOT NULL,
    created_at TIMESTAMP NOT NULL,
    notes TEXT,
    CONSTRAINT fk_loan_ledger_temp_loan FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE
);

INSERT INTO loan_ledger_temp (id, loan_id, entry_type, amount_inr, created_at, notes)
SELECT id, loan_id, entry_type, amount_inr, created_at, notes FROM loan_ledger;

DROP TABLE loan_ledger;

ALTER TABLE loan_ledger_temp RENAME TO loan_ledger;
