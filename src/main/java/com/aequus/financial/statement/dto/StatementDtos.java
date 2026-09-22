package com.aequus.financial.statement.dto;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class StatementDtos {

    private StatementDtos() {
        // Namespace
    }

    public record ParsedStatementItem(
            String id,
            LocalDate date,
            String description,
            FinancialType type,
            FinancialCategory category,
            BigDecimal amount,
            boolean isDuplicate,
            String rawLine
    ) {}

    public record StatementParseResponse(
            String filename,
            UUID accountId,
            String accountName,
            int totalCount,
            int newCount,
            int duplicateCount,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            List<ParsedStatementItem> items
    ) {}

    public record ImportItemRequest(
            @NotNull LocalDate date,
            @NotNull FinancialType type,
            @NotNull FinancialCategory category,
            @NotNull @Positive BigDecimal amount,
            String description
    ) {}

    public record ConfirmStatementImportRequest(
            @NotNull UUID accountId,
            @NotEmpty List<ImportItemRequest> items
    ) {}

    public record StatementImportResultResponse(
            UUID accountId,
            int importedCount,
            BigDecimal newBalance,
            String message
    ) {}
}
