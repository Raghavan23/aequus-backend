package com.aequus.financial.statement.parser;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.statement.dto.StatementDtos.ParsedStatementItem;
import com.aequus.financial.statement.exception.StatementParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvStatementParserTest {

    private CsvStatementParser parser;

    @BeforeEach
    void setUp() {
        CategoryInferenceEngine inferenceEngine = new CategoryInferenceEngine();
        parser = new CsvStatementParser(inferenceEngine);
    }

    @Test
    @DisplayName("Should parse CSV with separate Debit and Credit columns")
    void shouldParseCsvWithDebitAndCreditColumns() {
        // Given
        String csvContent = """
                Date,Description,Debit,Credit
                2026-09-01,Uber Trip,25.50,
                2026-09-02,Monthly Salary,,3500.00
                2026-09-03,Starbucks Coffee,5.75,
                """;
        InputStream stream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // When
        List<ParsedStatementItem> items = parser.parse(stream);

        // Then
        assertEquals(3, items.size());

        ParsedStatementItem item1 = items.get(0);
        assertEquals(LocalDate.of(2026, 9, 1), item1.date());
        assertEquals("Uber Trip", item1.description());
        assertEquals(FinancialType.EXPENSE, item1.type());
        assertEquals(FinancialCategory.TRAVEL, item1.category());
        assertEquals(new BigDecimal("25.50"), item1.amount());

        ParsedStatementItem item2 = items.get(1);
        assertEquals(LocalDate.of(2026, 9, 2), item2.date());
        assertEquals("Monthly Salary", item2.description());
        assertEquals(FinancialType.INCOME, item2.type());
        assertEquals(FinancialCategory.ACTIVE_INCOME, item2.category());
        assertEquals(new BigDecimal("3500.00"), item2.amount());

        ParsedStatementItem item3 = items.get(2);
        assertEquals(FinancialCategory.FOOD, item3.category());
    }

    @Test
    @DisplayName("Should parse CSV with single signed Amount column")
    void shouldParseCsvWithSingleSignedAmount() {
        // Given
        String csvContent = """
                Date,Narration,Amount
                05/09/2026,Netflix Subscription,-15.99
                10/09/2026,Client Consulting Fee,1200.00
                """;
        InputStream stream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // When
        List<ParsedStatementItem> items = parser.parse(stream);

        // Then
        assertEquals(2, items.size());

        ParsedStatementItem expense = items.get(0);
        assertEquals(FinancialType.EXPENSE, expense.type());
        assertEquals(FinancialCategory.ENTERTAINMENT, expense.category());
        assertEquals(new BigDecimal("15.99"), expense.amount());

        ParsedStatementItem income = items.get(1);
        assertEquals(FinancialType.INCOME, income.type());
        assertEquals(new BigDecimal("1200.00"), income.amount());
    }

    @Test
    @DisplayName("Should throw StatementParseException when CSV header is missing required columns")
    void shouldThrowWhenHeaderIsInvalid() {
        // Given
        String invalidCsv = """
                RandomHeader1,RandomHeader2
                Value1,Value2
                """;
        InputStream stream = new ByteArrayInputStream(invalidCsv.getBytes(StandardCharsets.UTF_8));

        // When / Then
        assertThrows(StatementParseException.class, () -> parser.parse(stream));
    }
}
