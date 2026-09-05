CREATE TABLE accounts (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    type                 VARCHAR(30) NOT NULL,
    currency             VARCHAR(3) NOT NULL DEFAULT 'INR',
    balance              NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    institution_name     VARCHAR(100),
    account_number_mask  VARCHAR(10),
    color                VARCHAR(30) DEFAULT '#3b82f6',
    icon                 VARCHAR(50) DEFAULT 'account_balance',
    is_archived          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_user_id_active ON accounts (user_id, is_archived);
