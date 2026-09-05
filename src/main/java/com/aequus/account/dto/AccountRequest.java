package com.aequus.account.dto;

import com.aequus.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must not exceed 100 characters")
        String name,

        @NotNull(message = "Account type is required")
        AccountType type,

        @Size(max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        BigDecimal balance,

        @Size(max = 100, message = "Institution name must not exceed 100 characters")
        String institutionName,

        @Size(max = 10, message = "Account number mask must not exceed 10 characters")
        String accountNumberMask,

        @Pattern(regexp = "^$|^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex code (e.g. #3b82f6)")
        String color,

        @Size(max = 50, message = "Icon name must not exceed 50 characters")
        String icon
) {
}
