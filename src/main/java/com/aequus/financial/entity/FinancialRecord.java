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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FinancialRecord() {
        // JPA
    }

    public FinancialRecord(UUID userId, FinancialType type, FinancialCategory category, BigDecimal amount) {
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    public void update(FinancialType type, FinancialCategory category, BigDecimal amount) {
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
