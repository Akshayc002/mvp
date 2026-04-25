-- Update loan_status_check to include EXTENSION_REQUESTED
ALTER TABLE loans DROP CONSTRAINT IF EXISTS loan_status_check;
ALTER TABLE loans ADD CONSTRAINT loan_status_check CHECK (status IN (
    'NEGOTIATING', 'AGREED', 'ACTIVE', 'DEFAULTED', 'CLOSED', 
    'AWAITING_SIGNATURES', 'AWAITING_FEE', 'CANCELLED',
    'AWAITING_COLLATERAL', 'COLLATERAL_LOCKED', 'REPAID',
    'MARGIN_CALL', 'LIQUIDATION_ELIGIBLE', 'LIQUIDATED',
    'DISPUTE_OPEN', 'EXTENSION_REQUESTED'
));

-- Create loan_extension_requests table
CREATE TABLE loan_extension_requests (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    new_tenure_days INT NOT NULL,
    new_interest_rate DECIMAL(5, 2),
    status VARCHAR(20) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_extension
        FOREIGN KEY(loan_id)
        REFERENCES loans(id)
        ON DELETE CASCADE
);
