-- V5: Make account_id mandatory on financial_records and implement soft delete across records & accounts

-- 1. Soft delete fields on financial_records
ALTER TABLE financial_records
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- 2. Cleanup orphan records if any exist before enforcing NOT NULL
DELETE FROM financial_records WHERE account_id IS NULL;

-- 3. Make account_id NOT NULL on financial_records
ALTER TABLE financial_records
    ALTER COLUMN account_id SET NOT NULL;

-- 4. Soft delete fields on accounts
ALTER TABLE accounts
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- 5. Indexes for performant active-record queries
CREATE INDEX idx_financial_records_user_active ON financial_records (user_id, is_deleted, created_at DESC);
CREATE INDEX idx_accounts_user_active ON accounts (user_id, is_deleted, created_at ASC);
