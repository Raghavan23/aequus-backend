package com.aequus.reconciliation.entity;

import com.aequus.client.entity.Client;
import com.aequus.organization.entity.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_jobs")
public class ReconciliationJob {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReconciliationJobStatus status = ReconciliationJobStatus.PENDING;

    @Column(name = "total_transactions", nullable = false)
    private int totalTransactions = 0;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount = 0;

    @Column(name = "anomaly_count", nullable = false)
    private int anomalyCount = 0;

    @Column(name = "unmatched_count", nullable = false)
    private int unmatchedCount = 0;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReconciliationJob() {
        // JPA
    }

    public ReconciliationJob(Organization organization, Client client) {
        this.organization = organization;
        this.client = client;
        this.status = ReconciliationJobStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Client getClient() {
        return client;
    }

    public ReconciliationJobStatus getStatus() {
        return status;
    }

    public void setStatus(ReconciliationJobStatus status) {
        this.status = status;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public int getUnmatchedCount() {
        return unmatchedCount;
    }

    public void setUnmatchedCount(int unmatchedCount) {
        this.unmatchedCount = unmatchedCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
