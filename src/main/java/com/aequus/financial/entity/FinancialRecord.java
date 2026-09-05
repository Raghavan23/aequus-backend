package com.aequus.financial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_records")
public class FinancialRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FinancialRecord() {
        // JPA
    }

    public FinancialRecord(UUID userId, UUID accountId, FinancialType type, FinancialCategory category, BigDecimal amount) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        this.userId = userId;
        this.accountId = accountId;
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    public void update(UUID accountId, FinancialType type, FinancialCategory category, BigDecimal amount) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        this.accountId = accountId;
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public FinancialType getType() {
        return type;
    }

    public FinancialCategory getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
