-- Fix tenures that were previously in days but are now interpreted as months
UPDATE loan_offers SET tenure_months = tenure_months / 30 WHERE tenure_months > 60;
UPDATE loans SET tenure_months = tenure_months / 30 WHERE tenure_months > 60;
UPDATE loan_extension_requests SET new_tenure_months = new_tenure_months / 30 WHERE new_tenure_months > 60;
