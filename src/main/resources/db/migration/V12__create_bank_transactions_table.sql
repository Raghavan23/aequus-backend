CREATE TABLE IF NOT EXISTS bank_transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    client_id           UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    transaction_date    DATE NOT NULL,
    narration           TEXT NOT NULL,
    reference_number    VARCHAR(100),
    type                VARCHAR(10) NOT NULL,  -- DEBIT, CREDIT
    amount              NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    balance_after       NUMERIC(15,2),
    accounting_head     VARCHAR(50),
    match_status        VARCHAR(20) NOT NULL DEFAULT 'UNMATCHED',
    matched_invoice_id  UUID,
    source_file         VARCHAR(255),
    raw_narration       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bank_txn_client ON bank_transactions(client_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_bank_txn_match ON bank_transactions(match_status);
CREATE INDEX IF NOT EXISTS idx_bank_txn_org ON bank_transactions(organization_id);
