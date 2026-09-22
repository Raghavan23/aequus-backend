CREATE TABLE IF NOT EXISTS audit_trail (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action              VARCHAR(50) NOT NULL,  -- MATCH_ACCEPTED, MATCH_REJECTED, INVOICE_UPLOADED, STATEMENT_IMPORTED, etc.
    entity_type         VARCHAR(50) NOT NULL,  -- BANK_TRANSACTION, INVOICE, MATCH_RESULT, RECONCILIATION_JOB
    entity_id           UUID NOT NULL,
    details             TEXT,                  -- JSON or formatted audit description
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_org ON audit_trail(organization_id, created_at DESC);
