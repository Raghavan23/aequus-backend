package com.aequus.financial.statement.parser;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.statement.dto.StatementDtos.ParsedStatementItem;
import com.aequus.financial.statement.exception.StatementParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class CsvStatementParser {

    private static final List<DateTimeFormatter> SUPPORTED_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    );

    private final CategoryInferenceEngine categoryInferenceEngine;

    public CsvStatementParser(CategoryInferenceEngine categoryInferenceEngine) {
        this.categoryInferenceEngine = categoryInferenceEngine;
    }

    public List<ParsedStatementItem> parse(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);

            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (headerMap == null || headerMap.isEmpty()) {
                throw new StatementParseException("Uploaded CSV contains no valid header row.");
            }

            ColumnIndices columns = resolveColumnIndices(headerMap);
            List<ParsedStatementItem> parsedItems = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                parseRecord(record, columns).ifPresent(parsedItems::add);
            }

            if (parsedItems.isEmpty()) {
                throw new StatementParseException("No valid transaction rows could be parsed from the CSV file.");
            }

            return Collections.unmodifiableList(parsedItems);
        } catch (StatementParseException e) {
            throw e;
        } catch (Exception e) {
            throw new StatementParseException("Failed to parse CSV statement: " + e.getMessage(), e);
        }
    }

    private Optional<ParsedStatementItem> parseRecord(CSVRecord record, ColumnIndices columns) {
        String rawDate = getFieldValue(record, columns.dateIndex);
        String description = getFieldValue(record, columns.descriptionIndex);

        if (rawDate == null || description == null) {
            return Optional.empty();
        }

        LocalDate date = parseDate(rawDate);
        if (date == null) {
            return Optional.empty();
        }

        AmountInfo amountInfo = resolveAmount(record, columns);
        if (amountInfo == null || amountInfo.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        FinancialCategory category = categoryInferenceEngine.inferCategory(description, amountInfo.type);
        String id = UUID.randomUUID().toString();

        return Optional.of(new ParsedStatementItem(
                id,
                date,
                description,
                amountInfo.type,
                category,
                amountInfo.amount,
                false,
                record.toString()
        ));
    }

    private AmountInfo resolveAmount(CSVRecord record, ColumnIndices columns) {
        if (columns.hasDebitAndCredit()) {
            return resolveFromDebitCredit(record, columns);
        }
        return resolveFromSingleAmount(record, columns);
    }

    private AmountInfo resolveFromDebitCredit(CSVRecord record, ColumnIndices columns) {
        BigDecimal debit = parseNumericValue(getFieldValue(record, columns.debitIndex));
        BigDecimal credit = parseNumericValue(getFieldValue(record, columns.creditIndex));

        if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) {
            return new AmountInfo(debit, FinancialType.EXPENSE);
        }
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
            return new AmountInfo(credit, FinancialType.INCOME);
        }
        return null;
    }

    private AmountInfo resolveFromSingleAmount(CSVRecord record, ColumnIndices columns) {
        BigDecimal rawAmount = parseNumericValue(getFieldValue(record, columns.amountIndex));
        if (rawAmount == null) {
            return null;
        }

        if (rawAmount.compareTo(BigDecimal.ZERO) < 0) {
            return new AmountInfo(rawAmount.abs(), FinancialType.EXPENSE);
        }
        return new AmountInfo(rawAmount, FinancialType.INCOME);
    }

    private LocalDate parseDate(String rawDate) {
        String cleaned = rawDate.trim();
        for (DateTimeFormatter formatter : SUPPORTED_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next pattern
            }
        }
        return null;
    }

    private BigDecimal parseNumericValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String sanitized = value.replaceAll("[$,€£₹\\s]", "").replace(",", "");
            return new BigDecimal(sanitized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getFieldValue(CSVRecord record, Integer index) {
        if (index == null || index < 0 || index >= record.size()) {
            return null;
        }
        String val = record.get(index);
        return (val != null && !val.isBlank()) ? val.trim() : null;
    }

    private ColumnIndices resolveColumnIndices(Map<String, Integer> headerMap) {
        Integer dateIndex = findMatchingHeaderIndex(headerMap, List.of("date", "trans date", "txn date", "posting date", "transaction date"));
        Integer descIndex = findMatchingHeaderIndex(headerMap, List.of("description", "narration", "details", "merchant", "memo", "payee", "particulars"));
        Integer debitIndex = findMatchingHeaderIndex(headerMap, List.of("debit", "withdrawal", "spent", "paid out", "dr"));
        Integer creditIndex = findMatchingHeaderIndex(headerMap, List.of("credit", "deposit", "received", "paid in", "cr"));
        Integer amountIndex = findMatchingHeaderIndex(headerMap, List.of("amount", "net amount", "transaction amount", "value"));

        if (dateIndex == null) {
            throw new StatementParseException("Could not identify Date column in CSV header.");
        }
        if (descIndex == null) {
            throw new StatementParseException("Could not identify Description/Merchant column in CSV header.");
        }
        if ((debitIndex == null || creditIndex == null) && amountIndex == null) {
            throw new StatementParseException("Could not identify Amount (or Debit/Credit) columns in CSV header.");
        }

        return new ColumnIndices(dateIndex, descIndex, debitIndex, creditIndex, amountIndex);
    }

    private Integer findMatchingHeaderIndex(Map<String, Integer> headerMap, List<String> synonyms) {
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String header = entry.getKey().toLowerCase(Locale.ROOT).trim();
            for (String synonym : synonyms) {
                if (header.contains(synonym)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private record ColumnIndices(
            Integer dateIndex,
            Integer descriptionIndex,
            Integer debitIndex,
            Integer creditIndex,
            Integer amountIndex
    ) {
        boolean hasDebitAndCredit() {
            return debitIndex != null && creditIndex != null;
        }
    }

    private record AmountInfo(BigDecimal amount, FinancialType type) {}
}
