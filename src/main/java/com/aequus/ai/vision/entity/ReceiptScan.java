package com.aequus.ai.vision.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receipt_scans")
public class ReceiptScan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "financial_record_id")
    private UUID financialRecordId;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(length = 150)
    private String merchant;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "tip_amount", precision = 15, scale = 2)
    private BigDecimal tipAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 10, nullable = false)
    private String currency = "INR";

    @Column(name = "suggested_category", length = 50)
    private String suggestedCategory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_extracted_json", columnDefinition = "JSONB")
    private String rawExtractedJson;

    @Column(length = 30, nullable = false)
    private String status = "PARSED"; // PENDING, PARSED, CONFIRMED, REJECTED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReceiptScan() {
        // JPA
    }

    public ReceiptScan(UUID userId, UUID accountId, String merchant, BigDecimal subtotal,
                       BigDecimal taxAmount, BigDecimal tipAmount, BigDecimal totalAmount,
                       String currency, String suggestedCategory, String rawExtractedJson) {
        this.userId = userId;
        this.accountId = accountId;
        this.merchant = merchant != null ? merchant.trim() : "Unknown Merchant";
        this.subtotal = subtotal;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        this.tipAmount = tipAmount != null ? tipAmount : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.currency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "INR";
        this.suggestedCategory = suggestedCategory;
        this.rawExtractedJson = rawExtractedJson;
        this.status = "PARSED";
    }

    public void confirm(UUID financialRecordId, UUID accountId) {
        this.financialRecordId = financialRecordId;
        this.accountId = accountId;
        this.status = "CONFIRMED";
    }

    public void reject() {
        this.status = "REJECTED";
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

    public UUID getFinancialRecordId() {
        return financialRecordId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMerchant() {
        return merchant;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTipAmount() {
        return tipAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public String getRawExtractedJson() {
        return rawExtractedJson;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
