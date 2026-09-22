package com.aequus.transaction.entity;

import com.aequus.client.entity.Client;
import com.aequus.organization.entity.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_transactions")
public class BankTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narration;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_head", length = 50)
    private AccountingHead accountingHead;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 20)
    private MatchStatus matchStatus = MatchStatus.UNMATCHED;

    @Column(name = "matched_invoice_id")
    private UUID matchedInvoiceId;

    @Column(name = "source_file", length = 255)
    private String sourceFile;

    @Column(name = "raw_narration", columnDefinition = "TEXT")
    private String rawNarration;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BankTransaction() {
        // JPA
    }

    public BankTransaction(Organization organization, Client client, LocalDate transactionDate,
                           String narration, String referenceNumber, TransactionType type,
                           BigDecimal amount, BigDecimal balanceAfter, AccountingHead accountingHead,
                           String sourceFile, String rawNarration) {
        this.organization = organization;
        this.client = client;
        this.transactionDate = transactionDate;
        this.narration = narration;
        this.referenceNumber = referenceNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.accountingHead = accountingHead != null ? accountingHead : AccountingHead.MISC;
        this.matchStatus = MatchStatus.UNMATCHED;
        this.sourceFile = sourceFile;
        this.rawNarration = rawNarration != null ? rawNarration : narration;
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public AccountingHead getAccountingHead() {
        return accountingHead;
    }

    public void setAccountingHead(AccountingHead accountingHead) {
        this.accountingHead = accountingHead;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public UUID getMatchedInvoiceId() {
        return matchedInvoiceId;
    }

    public void setMatchedInvoiceId(UUID matchedInvoiceId) {
        this.matchedInvoiceId = matchedInvoiceId;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getRawNarration() {
        return rawNarration;
    }

    public void setRawNarration(String rawNarration) {
        this.rawNarration = rawNarration;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
