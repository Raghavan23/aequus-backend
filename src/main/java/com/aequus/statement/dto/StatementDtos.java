package com.aequus.statement.dto;

import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class StatementDtos {

    private StatementDtos() {}

    public record ParsedBankStatementItem(
            String id,
            LocalDate date,
            String narration,
            String referenceNumber,
            TransactionType type,
            AccountingHead accountingHead,
            BigDecimal amount,
            BigDecimal balanceAfter,
            boolean isDuplicate,
            String rawLine
    ) {}

    public record BankStatementParseResponse(
            String filename,
            UUID clientId,
            String clientName,
            int totalCount,
            int newCount,
            int duplicateCount,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            List<ParsedBankStatementItem> items
    ) {}

    public record BankImportItemRequest(
            @NotNull LocalDate date,
            @NotNull String narration,
            String referenceNumber,
            @NotNull TransactionType type,
            AccountingHead accountingHead,
            @NotNull @Positive BigDecimal amount,
            BigDecimal balanceAfter,
            String rawLine
    ) {}

    public record ConfirmBankStatementImportRequest(
            @NotNull UUID clientId,
            @NotEmpty List<BankImportItemRequest> items,
            String sourceFilename
    ) {}

    public record BankStatementImportResultResponse(
            UUID clientId,
            int importedCount,
            String message
    ) {}
}
