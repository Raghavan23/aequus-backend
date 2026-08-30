package com.finz.financial.entity;

import com.finz.common.exception.BadRequestException;

import java.util.Set;

public enum FinancialCategory {
    ACTIVE_INCOME(FinancialType.INCOME),
    PASSIVE_INCOME(FinancialType.INCOME),

    FOOD(FinancialType.EXPENSE),
    TRAVEL(FinancialType.EXPENSE),
    ENTERTAINMENT(FinancialType.EXPENSE),
    EDUCATION(FinancialType.EXPENSE),
    CLOTHING(FinancialType.EXPENSE),
    MISCELLANEOUS(FinancialType.EXPENSE);

    private final FinancialType type;

    FinancialCategory(FinancialType type) {
        this.type = type;
    }

    public FinancialType getType() {
        return type;
    }

    public static final Set<FinancialCategory> INCOME_CATEGORIES =
            Set.of(ACTIVE_INCOME, PASSIVE_INCOME);

    public static final Set<FinancialCategory> EXPENSE_CATEGORIES =
            Set.of(FOOD, TRAVEL, ENTERTAINMENT, EDUCATION, CLOTHING, MISCELLANEOUS);

    /**
     * Ensures the category is a valid one for the given financial type.
     * e.g. INCOME + FOOD is rejected, EXPENSE + FOOD is accepted.
     */
    public void validateBelongsTo(FinancialType expectedType) {
        if (this.type != expectedType) {
            throw new BadRequestException(
                    "Category '" + this.name() + "' is not valid for type '" + expectedType + "'");
        }
    }
}
