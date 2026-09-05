package com.aequus.financial.service;

import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.dto.FinancialRecordRequest;
import com.aequus.financial.dto.FinancialRecordResponse;
import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.repository.FinancialRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FinancialRecordService financialRecordService;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void create_WhenIncomeWithAccount_ShouldCreditAccount() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        Account account = new Account(userId, "Savings", AccountType.SAVINGS, "USD",
                new BigDecimal("1000.00"), null, null, null, null);

        FinancialRecordRequest request = new FinancialRecordRequest(
                accountId,
                FinancialType.INCOME,
                FinancialCategory.ACTIVE_INCOME,
                new BigDecimal("500.00")
        );

        FinancialRecord saved = new FinancialRecord(userId, accountId, FinancialType.INCOME,
                FinancialCategory.ACTIVE_INCOME, new BigDecimal("500.00"));

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(saved);

        FinancialRecordResponse response = financialRecordService.create(request);

        assertThat(response.amount()).isEqualByComparingTo("500.00");
        assertThat(response.accountName()).isEqualTo("Savings");
        assertThat(account.getBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    void create_WhenExpenseWithAccount_ShouldDebitAccount() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        Account account = new Account(userId, "Savings", AccountType.SAVINGS, "USD",
                new BigDecimal("1000.00"), null, null, null, null);

        FinancialRecordRequest request = new FinancialRecordRequest(
                accountId,
                FinancialType.EXPENSE,
                FinancialCategory.FOOD,
                new BigDecimal("150.00")
        );

        FinancialRecord saved = new FinancialRecord(userId, accountId, FinancialType.EXPENSE,
                FinancialCategory.FOOD, new BigDecimal("150.00"));

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(saved);

        FinancialRecordResponse response = financialRecordService.create(request);

        assertThat(response.amount()).isEqualByComparingTo("150.00");
        assertThat(account.getBalance()).isEqualByComparingTo("850.00");
    }

    @Test
    void create_WhenCategoryMismatch_ShouldThrowBadRequest() {
        FinancialRecordRequest request = new FinancialRecordRequest(
                accountId,
                FinancialType.INCOME,
                FinancialCategory.FOOD, // Food is an expense category
                new BigDecimal("100.00")
        );

        assertThatThrownBy(() -> financialRecordService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not valid for type");

        verifyNoInteractions(financialRecordRepository);
    }

    @Test
    void delete_ShouldSoftDeleteAndRevertAccountBalance() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        UUID recordId = UUID.randomUUID();
        Account account = new Account(userId, "Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("1000.00"), null, null, null, null);
        FinancialRecord record = new FinancialRecord(userId, accountId, FinancialType.EXPENSE,
                FinancialCategory.FOOD, new BigDecimal("200.00"));

        when(financialRecordRepository.findByIdAndUserIdAndIsDeletedFalse(recordId, userId))
                .thenReturn(Optional.of(record));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        financialRecordService.delete(recordId);

        // Balance reverted: was 1000, debited 200 before, so deleting expense credits 200 -> 1200
        assertThat(account.getBalance()).isEqualByComparingTo("1200.00");
        assertThat(record.isDeleted()).isTrue();
        assertThat(record.getDeletedAt()).isNotNull();
        verify(financialRecordRepository).save(record);
    }
}
