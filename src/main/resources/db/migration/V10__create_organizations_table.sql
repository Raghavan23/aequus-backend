CREATE TABLE IF NOT EXISTS organizations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    gstin           VARCHAR(15),
    pan             VARCHAR(10),
    address         TEXT,
    phone           VARCHAR(20),
    plan_tier       VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Link users to organizations and add role
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS organization_id UUID REFERENCES organizations(id),
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'ADMIN';

CREATE INDEX IF NOT EXISTS idx_users_org ON users(organization_id);
