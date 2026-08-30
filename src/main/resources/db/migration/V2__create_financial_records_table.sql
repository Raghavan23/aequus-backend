CREATE TABLE financial_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    type            VARCHAR(20) NOT NULL,
    category        VARCHAR(30) NOT NULL,
    amount          NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT fk_financial_records_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_financial_records_user_id ON financial_records (user_id);
CREATE INDEX idx_financial_records_user_id_created_at ON financial_records (user_id, created_at DESC);
