ALTER TABLE financial_records
    ADD COLUMN account_id UUID,
    ADD CONSTRAINT fk_financial_records_account
        FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE SET NULL;

CREATE INDEX idx_financial_records_account_id ON financial_records (account_id);
