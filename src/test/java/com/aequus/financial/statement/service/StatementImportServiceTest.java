package com.aequus.financial.statement.service;

import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.repository.FinancialRecordRepository;
import com.aequus.financial.statement.dto.StatementDtos.*;
import com.aequus.financial.statement.parser.CategoryInferenceEngine;
import com.aequus.financial.statement.parser.CsvStatementParser;
import com.aequus.financial.statement.parser.PdfStatementParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementImportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private StatementImportService statementImportService;
    private UUID userId;
    private UUID accountId;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        CategoryInferenceEngine inferenceEngine = new CategoryInferenceEngine();
        CsvStatementParser csvParser = new CsvStatementParser(inferenceEngine);
        PdfStatementParser pdfParser = new PdfStatementParser(inferenceEngine);

        statementImportService = new StatementImportService(
                csvParser,
                pdfParser,
                accountRepository,
                financialRecordRepository,
                currentUserProvider
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        testAccount = new Account(userId, "Primary Checking", AccountType.CURRENT, "USD",
                new BigDecimal("1000.00"), "Chase", "1234", "#3b82f6", "account_balance");
        org.springframework.test.util.ReflectionTestUtils.setField(testAccount, "id", accountId);
    }

    @Test
    @DisplayName("Should parse statement and flag existing duplicate records")
    void shouldParseStatementAndFlagDuplicates() {
        // Given
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(testAccount));

        FinancialRecord existingFoodExpense = new FinancialRecord(
                userId, accountId, FinancialType.EXPENSE, FinancialCategory.FOOD, new BigDecimal("45.00")
        );
        when(financialRecordRepository.findAllByUserIdAndAccountIdAndIsDeletedFalse(userId, accountId))
                .thenReturn(List.of(existingFoodExpense));

        String csvContent = """
                Date,Description,Debit,Credit
                2026-09-01,Whole Foods Market,45.00,
                2026-09-02,Starbucks,6.50,
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        StatementParseResponse response = statementImportService.parseStatement(file, accountId);

        // Then
        assertEquals(2, response.totalCount());
        assertEquals(1, response.newCount());
        assertEquals(1, response.duplicateCount());

        ParsedStatementItem item1 = response.items().get(0);
        assertTrue(item1.isDuplicate(), "Whole foods $45 should be flagged as duplicate");

        ParsedStatementItem item2 = response.items().get(1);
        assertFalse(item2.isDuplicate(), "Starbucks $6.50 should be marked new");
    }

    @Test
    @DisplayName("Should batch import records and update account balance")
    void shouldConfirmBatchImportAndAdjustBalance() {
        // Given
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(testAccount));

        List<ImportItemRequest> importItems = List.of(
                new ImportItemRequest(LocalDate.now(), FinancialType.INCOME, FinancialCategory.ACTIVE_INCOME, new BigDecimal("500.00"), "Bonus"),
                new ImportItemRequest(LocalDate.now(), FinancialType.EXPENSE, FinancialCategory.FOOD, new BigDecimal("100.00"), "Groceries")
        );
        ConfirmStatementImportRequest request = new ConfirmStatementImportRequest(accountId, importItems);

        // When
        StatementImportResultResponse response = statementImportService.confirmImport(request);

        // Then
        assertEquals(2, response.importedCount());
        assertEquals(new BigDecimal("1400.00"), response.newBalance()); // 1000 + 500 - 100
        verify(financialRecordRepository, times(1)).saveAll(any());
        verify(accountRepository, times(1)).save(testAccount);
    }
}
