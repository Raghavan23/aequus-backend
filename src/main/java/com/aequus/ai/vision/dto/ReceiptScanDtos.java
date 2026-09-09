package com.aequus.ai.vision.dto;

import com.aequus.ai.vision.entity.ReceiptScan;
import com.aequus.financial.entity.FinancialCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReceiptScanDtos {

    public record LineItem(
            String name,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}

    public record ParsedReceiptResponse(
            UUID id,
            String merchant,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            Double taxRatePercentage,
            BigDecimal tipAmount,
            BigDecimal totalAmount,
            String currency,
            FinancialCategory suggestedCategory,
            UUID suggestedAccountId,
            List<LineItem> lineItems,
            String status,
            Instant createdAt
    ) {
        public static ParsedReceiptResponse from(ReceiptScan scan, List<LineItem> items, Double taxRate, UUID suggestedAccountId) {
            FinancialCategory category = null;
            if (scan.getSuggestedCategory() != null) {
                try {
                    category = FinancialCategory.valueOf(scan.getSuggestedCategory());
                } catch (Exception ignored) {}
            }

            return new ParsedReceiptResponse(
                    scan.getId(),
                    scan.getMerchant(),
                    scan.getSubtotal(),
                    scan.getTaxAmount(),
                    taxRate,
                    scan.getTipAmount(),
                    scan.getTotalAmount(),
                    scan.getCurrency(),
                    category != null ? category : FinancialCategory.FOOD,
                    suggestedAccountId,
                    items != null ? items : List.of(),
                    scan.getStatus(),
                    scan.getCreatedAt()
            );
        }
    }

    public record ConfirmReceiptRequest(
            @NotNull(message = "Account ID is required")
            UUID accountId,

            @NotNull(message = "Category is required")
            FinancialCategory category,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
            BigDecimal amount,

            String merchant,
            String notes
    ) {}

    public record ReceiptScanSummaryResponse(
            UUID id,
            String merchant,
            BigDecimal totalAmount,
            String currency,
            String suggestedCategory,
            String status,
            Instant createdAt
    ) {
        public static ReceiptScanSummaryResponse from(ReceiptScan scan) {
            return new ReceiptScanSummaryResponse(
                    scan.getId(),
                    scan.getMerchant(),
                    scan.getTotalAmount(),
                    scan.getCurrency(),
                    scan.getSuggestedCategory(),
                    scan.getStatus(),
                    scan.getCreatedAt()
            );
        }
    }
}
