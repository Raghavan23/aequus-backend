package com.aequus.reconciliation.dto;

import com.aequus.invoice.dto.InvoiceDtos.InvoiceResponse;
import com.aequus.reconciliation.entity.MatchResult;
import com.aequus.reconciliation.entity.MatchResultStatus;
import com.aequus.reconciliation.entity.MatchType;
import com.aequus.reconciliation.entity.ReconciliationJob;
import com.aequus.reconciliation.entity.ReconciliationJobStatus;
import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionResponse;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReconciliationDtos {

    public record ReconciliationRunRequest(
            @NotNull(message = "Client ID is required")
            UUID clientId
    ) {}

    public record MatchReviewRequest(
            @NotNull(message = "Action must be ACCEPT or REJECT")
            MatchResultStatus action
    ) {}

    public record ReconciliationJobResponse(
            UUID id,
            UUID organizationId,
            UUID clientId,
            String clientName,
            ReconciliationJobStatus status,
            int totalTransactions,
            int matchedCount,
            int anomalyCount,
            int unmatchedCount,
            double matchRatePercentage,
            Instant startedAt,
            Instant completedAt,
            Instant createdAt
    ) {
        public static ReconciliationJobResponse from(ReconciliationJob job) {
            double rate = job.getTotalTransactions() > 0
                    ? (double) job.getMatchedCount() / job.getTotalTransactions() * 100
                    : 0.0;

            return new ReconciliationJobResponse(
                    job.getId(),
                    job.getOrganization().getId(),
                    job.getClient().getId(),
                    job.getClient().getName(),
                    job.getStatus(),
                    job.getTotalTransactions(),
                    job.getMatchedCount(),
                    job.getAnomalyCount(),
                    job.getUnmatchedCount(),
                    Math.round(rate * 100.0) / 100.0,
                    job.getStartedAt(),
                    job.getCompletedAt(),
                    job.getCreatedAt()
            );
        }
    }

    public record MatchResultResponse(
            UUID id,
            UUID jobId,
            BankTransactionResponse transaction,
            InvoiceResponse invoice,
            MatchType matchType,
            BigDecimal confidenceScore,
            String reasoning,
            MatchResultStatus status,
            UUID reviewedByUserId,
            String reviewedByUserName,
            Instant reviewedAt,
            Instant createdAt
    ) {
        public static MatchResultResponse from(MatchResult mr) {
            return new MatchResultResponse(
                    mr.getId(),
                    mr.getJob().getId(),
                    BankTransactionResponse.from(mr.getTransaction()),
                    mr.getInvoice() != null ? InvoiceResponse.from(mr.getInvoice()) : null,
                    mr.getMatchType(),
                    mr.getConfidenceScore(),
                    mr.getReasoning(),
                    mr.getStatus(),
                    mr.getReviewedBy() != null ? mr.getReviewedBy().getId() : null,
                    mr.getReviewedBy() != null ? mr.getReviewedBy().getName() : null,
                    mr.getReviewedAt(),
                    mr.getCreatedAt()
            );
        }
    }

    public record ReconciliationWorkspaceResponse(
            ReconciliationJobResponse latestJob,
            List<MatchResultResponse> matches,
            List<BankTransactionResponse> unmatchedTransactions,
            List<InvoiceResponse> unmatchedInvoices
    ) {}
}
