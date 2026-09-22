package com.aequus.reconciliation.entity;

import com.aequus.invoice.entity.Invoice;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ReconciliationJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BankTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchResultStatus status = MatchResultStatus.SUGGESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MatchResult() {
        // JPA
    }

    public MatchResult(ReconciliationJob job, BankTransaction transaction, Invoice invoice,
                       MatchType matchType, BigDecimal confidenceScore, String reasoning,
                       MatchResultStatus status) {
        this.job = job;
        this.transaction = transaction;
        this.invoice = invoice;
        this.matchType = matchType;
        this.confidenceScore = confidenceScore;
        this.reasoning = reasoning;
        this.status = status != null ? status : MatchResultStatus.SUGGESTED;
    }

    public UUID getId() {
        return id;
    }

    public ReconciliationJob getJob() {
        return job;
    }

    public BankTransaction getTransaction() {
        return transaction;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public String getReasoning() {
        return reasoning;
    }

    public MatchResultStatus getStatus() {
        return status;
    }

    public void setStatus(MatchResultStatus status) {
        this.status = status;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
