CREATE TABLE early_access_waitlist (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_early_access_waitlist_email ON early_access_waitlist(email);
