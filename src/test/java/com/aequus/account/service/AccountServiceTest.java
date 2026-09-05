package com.aequus.account.service;

import com.aequus.account.dto.AccountRequest;
import com.aequus.account.dto.AccountResponse;
import com.aequus.account.dto.AccountSummaryResponse;
import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void create_ShouldSaveAndReturnAccount() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        AccountRequest request = new AccountRequest(
                "HDFC Savings",
                AccountType.SAVINGS,
                "INR",
                new BigDecimal("1500.00"),
                "HDFC Bank",
                "• 1234",
                "#3b82f6",
                "account_balance"
        );

        Account saved = new Account(userId, "HDFC Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("1500.00"), "HDFC Bank", "• 1234", "#3b82f6", "account_balance");

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.create(request);

        assertThat(response.name()).isEqualTo("HDFC Savings");
        assertThat(response.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.balance()).isEqualByComparingTo("1500.00");
    }

    @Test
    void getSummary_ShouldCalculateAssetsLiabilitiesAndNetWorth() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        Account current = new Account(userId, "Business Current", AccountType.CURRENT, "INR",
                new BigDecimal("5000.00"), null, null, null, null);
        Account savings = new Account(userId, "Emergency Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("10000.00"), null, null, null, null);
        Account salary = new Account(userId, "Corporate Salary", AccountType.SALARY, "INR",
                new BigDecimal("2000.00"), null, null, null, null);

        when(accountRepository.findAllByUserIdAndArchivedFalseOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(current, savings, salary));

        AccountSummaryResponse summary = accountService.getSummary();

        // Assets = 5000 + 10000 + 2000 = 17000
        assertThat(summary.totalAssets()).isEqualByComparingTo("17000.00");
        // Liabilities = 0
        assertThat(summary.totalLiabilities()).isEqualByComparingTo("0.00");
        // Net worth = 17000
        assertThat(summary.netWorth()).isEqualByComparingTo("17000.00");
        assertThat(summary.activeAccountsCount()).isEqualTo(3);
    }

    @Test
    void creditAccount_ShouldIncreaseBalance() {
        Account savings = new Account(userId, "Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("1000.00"), null, null, null, null);

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(savings));

        accountService.creditAccount(accountId, userId, new BigDecimal("500.00"));

        assertThat(savings.getBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    void debitAccount_ShouldDecreaseBalance() {
        Account savings = new Account(userId, "Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("1000.00"), null, null, null, null);

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(savings));

        accountService.debitAccount(accountId, userId, new BigDecimal("200.00"));

        assertThat(savings.getBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    void getById_WhenNotOwned_ShouldThrowResourceNotFound() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(accountId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
