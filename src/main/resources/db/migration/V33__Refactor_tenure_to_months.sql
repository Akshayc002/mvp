ALTER TABLE loan_offers RENAME COLUMN tenure_days TO tenure_months;
ALTER TABLE loans RENAME COLUMN tenure_days TO tenure_months;
ALTER TABLE loan_extension_requests RENAME COLUMN new_tenure_days TO new_tenure_months;
