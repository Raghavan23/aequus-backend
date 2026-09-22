CREATE TABLE IF NOT EXISTS reconciliation_jobs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    client_id           UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, RUNNING, COMPLETED, FAILED
    total_transactions  INT NOT NULL DEFAULT 0,
    matched_count       INT NOT NULL DEFAULT 0,
    anomaly_count       INT NOT NULL DEFAULT 0,
    unmatched_count     INT NOT NULL DEFAULT 0,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rec_jobs_client ON reconciliation_jobs(client_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rec_jobs_org ON reconciliation_jobs(organization_id);

CREATE TABLE IF NOT EXISTS match_results (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id              UUID NOT NULL REFERENCES reconciliation_jobs(id) ON DELETE CASCADE,
    transaction_id      UUID NOT NULL REFERENCES bank_transactions(id) ON DELETE CASCADE,
    invoice_id          UUID REFERENCES invoices(id) ON DELETE SET NULL,
    match_type          VARCHAR(20) NOT NULL,  -- EXACT, FUZZY, MANUAL, AI_SUGGESTED
    confidence_score    NUMERIC(5,2),          -- 0.00 to 100.00
    reasoning           TEXT,                  -- Explanation of why this match was suggested
    status              VARCHAR(20) NOT NULL DEFAULT 'SUGGESTED',  -- SUGGESTED, ACCEPTED, REJECTED
    reviewed_by         UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_match_results_job ON match_results(job_id);
CREATE INDEX IF NOT EXISTS idx_match_results_txn ON match_results(transaction_id);
