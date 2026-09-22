package com.aequus.invoice.entity;

import com.aequus.client.entity.Client;
import com.aequus.organization.entity.Organization;
import com.aequus.transaction.entity.MatchStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;

    @Column(name = "vendor_name", nullable = false, length = 200)
    private String vendorName;

    @Column(name = "vendor_gstin", length = 15)
    private String vendorGstin;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "gst_amount", precision = 15, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 20)
    private MatchStatus matchStatus = MatchStatus.UNMATCHED;

    @Column(name = "matched_txn_id")
    private UUID matchedTxnId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private InvoiceSourceType sourceType = InvoiceSourceType.MANUAL;

    @Column(name = "raw_extracted_json", columnDefinition = "TEXT")
    private String rawExtractedJson;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Invoice() {
        // JPA
    }

    public Invoice(Organization organization, Client client, String invoiceNumber,
                   String vendorName, String vendorGstin, LocalDate invoiceDate, LocalDate dueDate,
                   BigDecimal subtotal, BigDecimal gstAmount, BigDecimal totalAmount,
                   String currency, InvoiceSourceType sourceType, String rawExtractedJson, String imageUrl) {
        this.organization = organization;
        this.client = client;
        this.invoiceNumber = invoiceNumber;
        this.vendorName = vendorName;
        this.vendorGstin = vendorGstin;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.gstAmount = gstAmount;
        this.totalAmount = totalAmount;
        this.currency = (currency != null && !currency.isBlank()) ? currency : "INR";
        this.sourceType = sourceType != null ? sourceType : InvoiceSourceType.MANUAL;
        this.rawExtractedJson = rawExtractedJson;
        this.imageUrl = imageUrl;
        this.matchStatus = MatchStatus.UNMATCHED;
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorGstin() {
        return vendorGstin;
    }

    public void setVendorGstin(String vendorGstin) {
        this.vendorGstin = vendorGstin;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public UUID getMatchedTxnId() {
        return matchedTxnId;
    }

    public void setMatchedTxnId(UUID matchedTxnId) {
        this.matchedTxnId = matchedTxnId;
    }

    public InvoiceSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(InvoiceSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getRawExtractedJson() {
        return rawExtractedJson;
    }

    public void setRawExtractedJson(String rawExtractedJson) {
        this.rawExtractedJson = rawExtractedJson;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
