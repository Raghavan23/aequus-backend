package com.aequus.financial.dto;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Note: this DTO intentionally has no userId field. Ownership is always
 * derived from the authenticated security context, never from client input.
 */
public record FinancialRecordRequest(
        UUID accountId,

        @NotNull(message = "Type is required")
        FinancialType type,

        @NotNull(message = "Category is required")
        FinancialCategory category,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount
) {
    public FinancialRecordRequest(FinancialType type, FinancialCategory category, BigDecimal amount) {
        this(null, type, category, amount);
    }
}
