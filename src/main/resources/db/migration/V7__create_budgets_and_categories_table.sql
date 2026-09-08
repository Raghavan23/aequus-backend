CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    icon VARCHAR(50),
    color VARCHAR(20),
    is_discretionary BOOLEAN NOT NULL DEFAULT TRUE,
    priority_weight INT NOT NULL DEFAULT 5, -- 1 (essential/locked) to 10 (discretionary/flexible)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_category_name UNIQUE(user_id, name, type)
);

CREATE TABLE IF NOT EXISTS budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    monthly_limit NUMERIC(15, 2) NOT NULL,
    period_month INT NOT NULL, -- 1 to 12
    period_year INT NOT NULL,  -- e.g. 2026
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_category_period UNIQUE(user_id, category_id, period_year, period_month)
);

CREATE INDEX IF NOT EXISTS idx_budgets_user_period ON budgets(user_id, period_year, period_month);
CREATE INDEX IF NOT EXISTS idx_categories_user ON categories(user_id);
