package com.aequus.account.dto;

import java.math.BigDecimal;

public record AccountSummaryResponse(
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth,
        long activeAccountsCount
) {
}
