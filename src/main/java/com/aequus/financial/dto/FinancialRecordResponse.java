package com.aequus.financial.dto;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.entity.FinancialType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialRecordResponse(
        UUID id,
        UUID accountId,
        String accountName,
        FinancialType type,
        FinancialCategory category,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt
) {
    public static FinancialRecordResponse from(FinancialRecord record, String accountName) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getAccountId(),
                accountName,
                record.getType(),
                record.getCategory(),
                record.getAmount(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    public static FinancialRecordResponse from(FinancialRecord record) {
        return from(record, null);
    }
}
