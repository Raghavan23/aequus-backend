CREATE TABLE IF NOT EXISTS clients (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    gstin               VARCHAR(15),
    pan                 VARCHAR(10),
    tally_company_name  VARCHAR(200),
    contact_person      VARCHAR(150),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(20),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_clients_org ON clients(organization_id);
