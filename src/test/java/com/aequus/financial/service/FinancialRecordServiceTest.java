package com.aequus.financial.service;

import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
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
import java.util.List;
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
    void create_WhenAccountNotOwned_ShouldThrowResourceNotFoundException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        FinancialRecordRequest request = new FinancialRecordRequest(
                accountId,
                FinancialType.INCOME,
                FinancialCategory.ACTIVE_INCOME,
                new BigDecimal("500.00")
        );

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialRecordService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");

        verifyNoInteractions(financialRecordRepository);
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
    void getAllForCurrentUser_ShouldResolveActiveAndArchivedAccountNames() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        UUID activeAccId = UUID.randomUUID();
        UUID archivedAccId = UUID.randomUUID();

        Account activeAcc = new Account(userId, "Active Checking", AccountType.CURRENT, "INR",
                new BigDecimal("5000.00"), null, null, null, null);
        Account archivedAcc = new Account(userId, "Old Savings", AccountType.SAVINGS, "INR",
                new BigDecimal("100.00"), null, null, null, null);
        archivedAcc.setArchived(true);

        try {
            var idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activeAcc, activeAccId);
            idField.set(archivedAcc, archivedAccId);
        } catch (Exception ignored) {}

        FinancialRecord rec1 = new FinancialRecord(userId, activeAccId, FinancialType.EXPENSE,
                FinancialCategory.FOOD, new BigDecimal("50.00"));
        FinancialRecord rec2 = new FinancialRecord(userId, archivedAccId, FinancialType.INCOME,
                FinancialCategory.ACTIVE_INCOME, new BigDecimal("1000.00"));

        when(accountRepository.findAllByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(activeAcc, archivedAcc));
        when(financialRecordRepository.findAllByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(rec1, rec2));

        List<FinancialRecordResponse> result = financialRecordService.getAllForCurrentUser();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).accountName()).isEqualTo("Active Checking");
        assertThat(result.get(1).accountName()).isEqualTo("Old Savings (Archived)");
    }

    @Test
    void getById_WhenAccountIsArchived_ShouldReturnArchivedLabel() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        UUID recordId = UUID.randomUUID();
        FinancialRecord record = new FinancialRecord(userId, accountId, FinancialType.EXPENSE,
                FinancialCategory.FOOD, new BigDecimal("50.00"));

        Account archivedAcc = new Account(userId, "Old Bank", AccountType.SAVINGS, "INR",
                new BigDecimal("0.00"), null, null, null, null);
        archivedAcc.softDelete();

        when(financialRecordRepository.findByIdAndUserIdAndIsDeletedFalse(recordId, userId))
                .thenReturn(Optional.of(record));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(archivedAcc));

        FinancialRecordResponse response = financialRecordService.getById(recordId);

        assertThat(response.accountName()).isEqualTo("Old Bank (Archived)");
    }

    @Test
    void getById_WhenRecordNotFound_ShouldThrowResourceNotFoundException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        UUID recordId = UUID.randomUUID();
        when(financialRecordRepository.findByIdAndUserIdAndIsDeletedFalse(recordId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialRecordService.getById(recordId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Financial record not found");
    }

    @Test
    void update_WhenValid_ShouldRevertOldAndApplyNewBalance() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        UUID recordId = UUID.randomUUID();
        UUID oldAccountId = UUID.randomUUID();
        UUID newAccountId = UUID.randomUUID();

        Account oldAccount = new Account(userId, "Old Account", AccountType.SAVINGS, "INR",
                new BigDecimal("800.00"), null, null, null, null);
        Account newAccount = new Account(userId, "New Account", AccountType.SAVINGS, "INR",
                new BigDecimal("2000.00"), null, null, null, null);

        FinancialRecord existingRecord = new FinancialRecord(userId, oldAccountId, FinancialType.EXPENSE,
                FinancialCategory.FOOD, new BigDecimal("200.00"));

        when(financialRecordRepository.findByIdAndUserIdAndIsDeletedFalse(recordId, userId))
                .thenReturn(Optional.of(existingRecord));
        when(accountRepository.findByIdAndUserId(oldAccountId, userId))
                .thenReturn(Optional.of(oldAccount));
        when(accountRepository.findByIdAndUserId(newAccountId, userId))
                .thenReturn(Optional.of(newAccount));

        FinancialRecordRequest request = new FinancialRecordRequest(
                newAccountId,
                FinancialType.EXPENSE,
                FinancialCategory.ENTERTAINMENT,
                new BigDecimal("300.00")
        );

        FinancialRecordResponse response = financialRecordService.update(recordId, request);

        // Old account had 200 debited, reverting adds 200 -> 1000
        assertThat(oldAccount.getBalance()).isEqualByComparingTo("1000.00");
        // New account debited by 300 -> 1700
        assertThat(newAccount.getBalance()).isEqualByComparingTo("1700.00");
        assertThat(response.accountName()).isEqualTo("New Account");
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
