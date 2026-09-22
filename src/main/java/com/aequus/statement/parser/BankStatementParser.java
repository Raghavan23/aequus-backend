package com.aequus.statement.parser;

import com.aequus.statement.dto.StatementDtos.ParsedBankStatementItem;
import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.TransactionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BankStatementParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    );

    public List<ParsedBankStatementItem> parseCsv(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);

            Map<String, Integer> headers = parser.getHeaderMap();
            List<ParsedBankStatementItem> items = new ArrayList<>();

            for (CSVRecord record : parser) {
                parseCsvRecord(record, headers).ifPresent(items::add);
            }

            return items;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing bank CSV statement: " + e.getMessage(), e);
        }
    }

    public List<ParsedBankStatementItem> parsePdf(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            return parsePdfLines(text);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing bank PDF statement: " + e.getMessage(), e);
        }
    }

    private Optional<ParsedBankStatementItem> parseCsvRecord(CSVRecord record, Map<String, Integer> headers) {
        String dateStr = findValue(record, headers, "date", "txn date", "transaction date", "value date");
        String narration = findValue(record, headers, "narration", "description", "particulars", "remarks", "details");
        String refNum = findValue(record, headers, "ref", "reference", "chq/ref no", "cheque no", "ref no", "utr");
        String withdrawal = findValue(record, headers, "debit", "withdrawal", "dr", "withdrawals");
        String deposit = findValue(record, headers, "credit", "deposit", "cr", "deposits");
        String balanceStr = findValue(record, headers, "balance", "closing balance", "bal");

        if (dateStr == null || narration == null) return Optional.empty();

        LocalDate date = parseDate(dateStr);
        if (date == null) return Optional.empty();

        TransactionType type = TransactionType.DEBIT;
        BigDecimal amount = BigDecimal.ZERO;

        if (withdrawal != null && !withdrawal.isBlank() && parseAmount(withdrawal).compareTo(BigDecimal.ZERO) > 0) {
            type = TransactionType.DEBIT;
            amount = parseAmount(withdrawal);
        } else if (deposit != null && !deposit.isBlank() && parseAmount(deposit).compareTo(BigDecimal.ZERO) > 0) {
            type = TransactionType.CREDIT;
            amount = parseAmount(deposit);
        } else {
            String genericAmount = findValue(record, headers, "amount", "txn amount");
            if (genericAmount != null) {
                amount = parseAmount(genericAmount);
                String typeStr = findValue(record, headers, "type", "dr/cr");
                if (typeStr != null && (typeStr.equalsIgnoreCase("CR") || typeStr.equalsIgnoreCase("CREDIT"))) {
                    type = TransactionType.CREDIT;
                }
            }
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) return Optional.empty();

        BigDecimal balance = (balanceStr != null) ? parseAmount(balanceStr) : null;
        AccountingHead head = inferHead(narration, type);

        return Optional.of(new ParsedBankStatementItem(
                UUID.randomUUID().toString(),
                date,
                narration,
                refNum,
                type,
                head,
                amount,
                balance,
                false,
                record.toString()
        ));
    }

    private List<ParsedBankStatementItem> parsePdfLines(String text) {
        List<ParsedBankStatementItem> items = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        Pattern linePattern = Pattern.compile("(\\d{2}[-/]\\d{2}[-/]\\d{4}|\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4})\\s+(.+?)\\s+([0-9,]+\\.\\d{2})\\s*(CR|DR)?");

        for (String line : lines) {
            Matcher m = linePattern.matcher(line.trim());
            if (m.find()) {
                String dateStr = m.group(1);
                String narration = m.group(2).trim();
                String amountStr = m.group(3);
                String crDr = m.group(4);

                LocalDate date = parseDate(dateStr);
                if (date != null) {
                    BigDecimal amount = parseAmount(amountStr);
                    TransactionType type = (crDr != null && crDr.equalsIgnoreCase("CR")) ? TransactionType.CREDIT : TransactionType.DEBIT;
                    AccountingHead head = inferHead(narration, type);

                    items.add(new ParsedBankStatementItem(
                            UUID.randomUUID().toString(),
                            date,
                            narration,
                            null,
                            type,
                            head,
                            amount,
                            null,
                            false,
                            line
                    ));
                }
            }
        }
        return items;
    }

    private String findValue(CSVRecord record, Map<String, Integer> headers, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase().contains(key)) {
                    int index = entry.getValue();
                    if (index < record.size()) {
                        String val = record.get(index);
                        if (val != null && !val.isBlank()) return val.trim();
                    }
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(val.trim(), fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal parseAmount(String val) {
        if (val == null) return BigDecimal.ZERO;
        try {
            String cleaned = val.replaceAll("[^0-9.-]", "");
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private AccountingHead inferHead(String narration, TransactionType type) {
        String lower = (narration != null ? narration : "").toLowerCase();
        if (lower.contains("salary") || lower.contains("payroll")) return AccountingHead.SALARY;
        if (lower.contains("rent") || lower.contains("lease")) return AccountingHead.RENT;
        if (lower.contains("gst") || lower.contains("tax") || lower.contains("tds")) return (type == TransactionType.CREDIT) ? AccountingHead.GST_OUTPUT : AccountingHead.GST_INPUT;
        if (lower.contains("interest")) return AccountingHead.INTEREST;
        if (lower.contains("charge") || lower.contains("fee") || lower.contains("sms chg")) return AccountingHead.BANK_CHARGES;
        if (lower.contains("electric") || lower.contains("bescom") || lower.contains("power") || lower.contains("water") || lower.contains("telecom") || lower.contains("airtel") || lower.contains("jio")) return AccountingHead.UTILITIES;
        if (type == TransactionType.CREDIT) return AccountingHead.SALES;
        return AccountingHead.PURCHASE;
    }
}
