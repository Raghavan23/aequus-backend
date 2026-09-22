package com.aequus.invoice.dto;

import com.aequus.invoice.entity.Invoice;
import com.aequus.invoice.entity.InvoiceSourceType;
import com.aequus.transaction.entity.MatchStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class InvoiceDtos {

    public record InvoiceRequest(
            @NotNull(message = "Client ID is required")
            UUID clientId,

            @NotBlank(message = "Invoice number is required")
            @Size(max = 100, message = "Invoice number must not exceed 100 characters")
            String invoiceNumber,

            @NotBlank(message = "Vendor name is required")
            @Size(max = 200, message = "Vendor name must not exceed 200 characters")
            String vendorName,

            @Size(max = 15, message = "GSTIN must not exceed 15 characters")
            String vendorGstin,

            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal gstAmount,

            @NotNull(message = "Total amount is required")
            @DecimalMin(value = "0.01", message = "Total amount must be strictly positive")
            BigDecimal totalAmount,

            String currency,
            InvoiceSourceType sourceType,
            String rawExtractedJson,
            String imageUrl
    ) {}

    public record InvoiceResponse(
            UUID id,
            UUID organizationId,
            UUID clientId,
            String clientName,
            String invoiceNumber,
            String vendorName,
            String vendorGstin,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal gstAmount,
            BigDecimal totalAmount,
            String currency,
            MatchStatus matchStatus,
            UUID matchedTxnId,
            InvoiceSourceType sourceType,
            String rawExtractedJson,
            String imageUrl,
            Instant createdAt
    ) {
        public static InvoiceResponse from(Invoice inv) {
            return new InvoiceResponse(
                    inv.getId(),
                    inv.getOrganization().getId(),
                    inv.getClient().getId(),
                    inv.getClient().getName(),
                    inv.getInvoiceNumber(),
                    inv.getVendorName(),
                    inv.getVendorGstin(),
                    inv.getInvoiceDate(),
                    inv.getDueDate(),
                    inv.getSubtotal(),
                    inv.getGstAmount(),
                    inv.getTotalAmount(),
                    inv.getCurrency(),
                    inv.getMatchStatus(),
                    inv.getMatchedTxnId(),
                    inv.getSourceType(),
                    inv.getRawExtractedJson(),
                    inv.getImageUrl(),
                    inv.getCreatedAt()
            );
        }
    }

    public record ParsedInvoiceItem(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            BigDecimal gstRate
    ) {}

    public record ParsedInvoiceResponse(
            UUID invoiceId,
            UUID clientId,
            String invoiceNumber,
            String vendorName,
            String vendorGstin,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal gstAmount,
            BigDecimal totalAmount,
            String currency,
            List<ParsedInvoiceItem> items,
            String rawJson,
            double confidenceScore
    ) {}
}
