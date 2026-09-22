package com.aequus.transaction.dto;

import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.MatchStatus;
import com.aequus.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class BankTransactionDtos {

    public record BankTransactionRequest(
            @NotNull(message = "Client ID is required")
            UUID clientId,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @NotBlank(message = "Narration is required")
            String narration,

            String referenceNumber,

            @NotNull(message = "Transaction type (DEBIT/CREDIT) is required")
            TransactionType type,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
            BigDecimal amount,

            BigDecimal balanceAfter,
            AccountingHead accountingHead
    ) {}

    public record BankTransactionResponse(
            UUID id,
            UUID organizationId,
            UUID clientId,
            String clientName,
            LocalDate transactionDate,
            String narration,
            String referenceNumber,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            AccountingHead accountingHead,
            MatchStatus matchStatus,
            UUID matchedInvoiceId,
            String sourceFile,
            Instant createdAt
    ) {
        public static BankTransactionResponse from(BankTransaction tx) {
            return new BankTransactionResponse(
                    tx.getId(),
                    tx.getOrganization().getId(),
                    tx.getClient().getId(),
                    tx.getClient().getName(),
                    tx.getTransactionDate(),
                    tx.getNarration(),
                    tx.getReferenceNumber(),
                    tx.getType(),
                    tx.getAmount(),
                    tx.getBalanceAfter(),
                    tx.getAccountingHead(),
                    tx.getMatchStatus(),
                    tx.getMatchedInvoiceId(),
                    tx.getSourceFile(),
                    tx.getCreatedAt()
            );
        }
    }

    public record TransactionSummaryResponse(
            long totalCount,
            long matchedCount,
            long anomalyCount,
            long unmatchedCount,
            BigDecimal totalDebitVolume,
            BigDecimal totalCreditVolume,
            double matchRatePercentage
    ) {}
}
