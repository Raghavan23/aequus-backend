package com.finz.financial.dto;

import com.finz.financial.entity.FinancialCategory;
import com.finz.financial.entity.FinancialRecord;
import com.finz.financial.entity.FinancialType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialRecordResponse(
        UUID id,
        FinancialType type,
        FinancialCategory category,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt
) {
    public static FinancialRecordResponse from(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getType(),
                record.getCategory(),
                record.getAmount(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
