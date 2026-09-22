package com.aequus.financial.statement.parser;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.statement.dto.StatementDtos.ParsedStatementItem;
import com.aequus.financial.statement.exception.StatementParseException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfStatementParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}|\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}|\\d{1,2}[-\\s][A-Za-z]{3}[-\\s]\\d{2,4})\\b"
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "\\b([+-]?\\$?\\s*\\d{1,3}(?:,\\d{3})*\\.\\d{2})\\s*(CR|DR)?\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final CategoryInferenceEngine categoryInferenceEngine;

    public PdfStatementParser(CategoryInferenceEngine categoryInferenceEngine) {
        this.categoryInferenceEngine = categoryInferenceEngine;
    }

    public List<ParsedStatementItem> parse(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullText = textStripper.getText(document);

            if (fullText == null || fullText.isBlank()) {
                throw new StatementParseException("The uploaded PDF does not contain extractable text (it might be a scanned image).");
            }

            List<ParsedStatementItem> parsedItems = parseLines(fullText.lines().toList());

            if (parsedItems.isEmpty()) {
                throw new StatementParseException("Could not detect standard transaction patterns in the PDF statement.");
            }

            return Collections.unmodifiableList(parsedItems);
        } catch (StatementParseException e) {
            throw e;
        } catch (Exception e) {
            throw new StatementParseException("Failed to parse PDF statement: " + e.getMessage(), e);
        }
    }

    private List<ParsedStatementItem> parseLines(List<String> lines) {
        List<ParsedStatementItem> items = new ArrayList<>();

        for (String line : lines) {
            parseLine(line).ifPresent(items::add);
        }

        return items;
    }

    private Optional<ParsedStatementItem> parseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.length() < 10) {
            return Optional.empty();
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(trimmed);
        if (!dateMatcher.find()) {
            return Optional.empty();
        }

        String rawDate = dateMatcher.group(1);
        LocalDate date = parseDate(rawDate);
        if (date == null) {
            return Optional.empty();
        }

        // Find all amounts in the line
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(trimmed);
        List<ParsedAmount> amounts = new ArrayList<>();
        while (amountMatcher.find()) {
            String rawAmount = amountMatcher.group(1);
            String crDr = amountMatcher.group(2);
            BigDecimal value = parseNumericValue(rawAmount);
            if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
                amounts.add(new ParsedAmount(value, crDr, amountMatcher.start(), amountMatcher.end()));
            }
        }

        if (amounts.isEmpty()) {
            return Optional.empty();
        }

        // Use the first valid amount on the transaction row
        ParsedAmount primaryAmount = amounts.get(0);
        FinancialType type = determineType(primaryAmount, trimmed);
        BigDecimal amount = primaryAmount.value.abs();

        String description = extractDescription(trimmed, dateMatcher.end(), primaryAmount.startIndex);
        if (description.isBlank()) {
            description = "Transaction on " + date;
        }

        FinancialCategory category = categoryInferenceEngine.inferCategory(description, type);
        String id = UUID.randomUUID().toString();

        return Optional.of(new ParsedStatementItem(
                id,
                date,
                description,
                type,
                category,
                amount,
                false,
                trimmed
        ));
    }

    private FinancialType determineType(ParsedAmount amount, String line) {
        if ("CR".equalsIgnoreCase(amount.indicator) || amount.value.compareTo(BigDecimal.ZERO) > 0 && line.toLowerCase(Locale.ROOT).contains("credit")) {
            return FinancialType.INCOME;
        }
        if ("DR".equalsIgnoreCase(amount.indicator) || line.toLowerCase(Locale.ROOT).contains("debit")) {
            return FinancialType.EXPENSE;
        }
        if (amount.value.compareTo(BigDecimal.ZERO) < 0) {
            return FinancialType.EXPENSE;
        }
        return FinancialType.EXPENSE;
    }

    private String extractDescription(String line, int dateEnd, int amountStart) {
        if (amountStart > dateEnd) {
            return line.substring(dateEnd, amountStart)
                    .replaceAll("[^a-zA-Z0-9\\s\\-.,&]", " ")
                    .trim()
                    .replaceAll("\\s{2,}", " ");
        }
        return "";
    }

    private LocalDate parseDate(String rawDate) {
        String cleaned = rawDate.trim().replace('.', '-').replace('/', '-');
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
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

    private record ParsedAmount(BigDecimal value, String indicator, int startIndex, int endIndex) {}
}
