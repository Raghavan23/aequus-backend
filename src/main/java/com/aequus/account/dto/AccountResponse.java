package com.aequus.account.dto;

import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        String currency,
        BigDecimal balance,
        String institutionName,
        String accountNumberMask,
        String color,
        String icon,
        boolean isArchived,
        Instant createdAt,
        Instant updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getCurrency(),
                account.getBalance(),
                account.getInstitutionName(),
                account.getAccountNumberMask(),
                account.getColor(),
                account.getIcon(),
                account.isArchived(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
