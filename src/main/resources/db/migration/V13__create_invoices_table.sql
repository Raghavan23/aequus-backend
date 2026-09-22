CREATE TABLE IF NOT EXISTS invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    client_id           UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    invoice_number      VARCHAR(100) NOT NULL,
    vendor_name         VARCHAR(200) NOT NULL,
    vendor_gstin        VARCHAR(15),
    invoice_date        DATE,
    due_date            DATE,
    subtotal            NUMERIC(15,2),
    gst_amount          NUMERIC(15,2),
    total_amount        NUMERIC(15,2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    match_status        VARCHAR(20) NOT NULL DEFAULT 'UNMATCHED',
    matched_txn_id      UUID,
    source_type         VARCHAR(20) NOT NULL DEFAULT 'MANUAL',  -- MANUAL, VLM_SCAN, TALLY
    raw_extracted_json  TEXT,
    image_url           TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_invoices_client ON invoices(client_id, invoice_date);
CREATE INDEX IF NOT EXISTS idx_invoices_match ON invoices(match_status);
CREATE INDEX IF NOT EXISTS idx_invoices_org ON invoices(organization_id);
