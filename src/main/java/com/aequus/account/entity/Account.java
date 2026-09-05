package com.aequus.account.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "institution_name", length = 100)
    private String institutionName;

    @Column(name = "account_number_mask", length = 10)
    private String accountNumberMask;

    @Column(length = 30)
    private String color = "#3b82f6";

    @Column(length = 50)
    private String icon = "account_balance";

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

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

    protected Account() {
        // JPA
    }

    public Account(UUID userId, String name, AccountType type, String currency, BigDecimal balance,
                   String institutionName, String accountNumberMask, String color, String icon) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.currency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "INR";
        this.balance = (balance != null) ? balance : BigDecimal.ZERO;
        this.institutionName = institutionName;
        this.accountNumberMask = accountNumberMask;
        this.color = (color != null && !color.isBlank()) ? color : "#3b82f6";
        this.icon = (icon != null && !icon.isBlank()) ? icon : "account_balance";
    }

    public void update(String name, AccountType type, String currency, BigDecimal balance,
                       String institutionName, String accountNumberMask, String color, String icon) {
        this.name = name;
        this.type = type;
        if (currency != null && !currency.isBlank()) {
            this.currency = currency.toUpperCase();
        }
        if (balance != null) {
            this.balance = balance;
        }
        this.institutionName = institutionName;
        this.accountNumberMask = accountNumberMask;
        if (color != null && !color.isBlank()) {
            this.color = color;
        }
        if (icon != null && !icon.isBlank()) {
            this.icon = icon;
        }
    }

    /**
     * Credits the account (increases asset balance, or decreases liability).
     */
    public void credit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
        }
    }

    /**
     * Debits the account (decreases asset balance, or increases liability).
     */
    public void debit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.subtract(amount);
        }
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getAccountNumberMask() {
        return accountNumberMask;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isArchived() {
        return archived;
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
