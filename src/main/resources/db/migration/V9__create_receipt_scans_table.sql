CREATE TABLE IF NOT EXISTS receipt_scans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID REFERENCES accounts(id) ON DELETE SET NULL,
    financial_record_id UUID REFERENCES financial_records(id) ON DELETE SET NULL,
    image_url TEXT,
    merchant VARCHAR(150),
    subtotal NUMERIC(15, 2),
    tax_amount NUMERIC(15, 2),
    tip_amount NUMERIC(15, 2),
    total_amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    suggested_category VARCHAR(50),
    raw_extracted_json JSONB,
    status VARCHAR(30) NOT NULL DEFAULT 'PARSED', -- PENDING, PARSED, CONFIRMED, REJECTED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_receipt_scans_user ON receipt_scans(user_id);
CREATE INDEX IF NOT EXISTS idx_receipt_scans_status ON receipt_scans(status);
