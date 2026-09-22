package com.aequus.reconciliation.engine;

import com.aequus.invoice.entity.Invoice;
import com.aequus.reconciliation.entity.MatchType;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class DeterministicMatcher {

    public record MatchCandidate(
            BankTransaction transaction,
            Invoice invoice,
            MatchType matchType,
            BigDecimal confidenceScore,
            String reasoning,
            boolean autoAccept
    ) {}

    public List<MatchCandidate> findMatches(List<BankTransaction> transactions, List<Invoice> invoices) {
        List<MatchCandidate> matches = new ArrayList<>();
        Set<UUID> matchedTxnIds = new HashSet<>();
        Set<UUID> matchedInvoiceIds = new HashSet<>();

        // 1. Pass: Rule 1 - Exact Amount & Exact/Close Date (±2 days)
        for (BankTransaction txn : transactions) {
            if (matchedTxnIds.contains(txn.getId())) continue;

            for (Invoice inv : invoices) {
                if (matchedInvoiceIds.contains(inv.getId())) continue;

                if (isExactAmountMatch(txn, inv)) {
                    long daysDiff = getDaysDifference(txn, inv);
                    if (daysDiff <= 2) {
                        BigDecimal confidence = (daysDiff == 0) ? BigDecimal.valueOf(99.00) : BigDecimal.valueOf(95.00);
                        String reason = String.format("Exact amount ₹%s matched within %d day(s) of invoice date.",
                                txn.getAmount(), daysDiff);

                        matches.add(new MatchCandidate(txn, inv, MatchType.EXACT, confidence, reason, true));
                        matchedTxnIds.add(txn.getId());
                        matchedInvoiceIds.add(inv.getId());
                        break;
                    }
                }
            }
        }

        // 2. Pass: Rule 2 - Exact Amount & Date Window (±7 days)
        for (BankTransaction txn : transactions) {
            if (matchedTxnIds.contains(txn.getId())) continue;

            for (Invoice inv : invoices) {
                if (matchedInvoiceIds.contains(inv.getId())) continue;

                if (isExactAmountMatch(txn, inv)) {
                    long daysDiff = getDaysDifference(txn, inv);
                    if (daysDiff <= 7) {
                        BigDecimal confidence = BigDecimal.valueOf(88.00);
                        String reason = String.format("Exact amount ₹%s matched within %d days window. Review suggested.",
                                txn.getAmount(), daysDiff);

                        matches.add(new MatchCandidate(txn, inv, MatchType.EXACT, confidence, reason, false));
                        matchedTxnIds.add(txn.getId());
                        matchedInvoiceIds.add(inv.getId());
                        break;
                    }
                }
            }
        }

        // 3. Pass: Rule 3 - Fuzzy Amount (±2% TDS/discount variance) + Narration keyword match
        for (BankTransaction txn : transactions) {
            if (matchedTxnIds.contains(txn.getId())) continue;

            for (Invoice inv : invoices) {
                if (matchedInvoiceIds.contains(inv.getId())) continue;

                boolean narrationMatched = containsVendorOrInvoiceNumber(txn, inv);
                BigDecimal amountDiffPercent = calculateAmountDiffPercentage(txn.getAmount(), inv.getTotalAmount());

                if (amountDiffPercent.compareTo(BigDecimal.valueOf(2.0)) <= 0 || narrationMatched) {
                    BigDecimal confidence;
                    String reason;

                    if (amountDiffPercent.compareTo(BigDecimal.valueOf(2.0)) <= 0 && narrationMatched) {
                        confidence = BigDecimal.valueOf(85.00);
                        reason = String.format("Fuzzy match: Narration matches vendor '%s' and amount within %.2f%% (possible TDS/discount).",
                                inv.getVendorName(), amountDiffPercent.doubleValue());
                    } else if (narrationMatched) {
                        confidence = BigDecimal.valueOf(75.00);
                        reason = String.format("Semantic match: Bank narration contains vendor name '%s' or invoice #%s.",
                                inv.getVendorName(), inv.getInvoiceNumber());
                    } else {
                        confidence = BigDecimal.valueOf(70.00);
                        reason = String.format("Near amount match: Variance of %.2f%% from invoice ₹%s.",
                                amountDiffPercent.doubleValue(), inv.getTotalAmount());
                    }

                    matches.add(new MatchCandidate(txn, inv, MatchType.FUZZY, confidence, reason, false));
                    matchedTxnIds.add(txn.getId());
                    matchedInvoiceIds.add(inv.getId());
                    break;
                }
            }
        }

        return matches;
    }

    private boolean isExactAmountMatch(BankTransaction txn, Invoice inv) {
        if (txn.getAmount() == null || inv.getTotalAmount() == null) return false;
        return txn.getAmount().compareTo(inv.getTotalAmount()) == 0;
    }

    private long getDaysDifference(BankTransaction txn, Invoice inv) {
        if (txn.getTransactionDate() == null || inv.getInvoiceDate() == null) return 999;
        return Math.abs(ChronoUnit.DAYS.between(txn.getTransactionDate(), inv.getInvoiceDate()));
    }

    private boolean containsVendorOrInvoiceNumber(BankTransaction txn, Invoice inv) {
        String narration = (txn.getNarration() != null ? txn.getNarration() : "").toLowerCase();

        if (inv.getInvoiceNumber() != null && !inv.getInvoiceNumber().isBlank()) {
            String invNum = inv.getInvoiceNumber().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (!invNum.isEmpty() && narration.contains(invNum)) {
                return true;
            }
        }

        if (inv.getVendorName() != null && !inv.getVendorName().isBlank()) {
            String[] words = inv.getVendorName().toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.length() > 3 && !List.of("pvt", "ltd", "private", "limited", "corp", "inc", "the", "and").contains(word)) {
                    if (narration.contains(word)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BigDecimal calculateAmountDiffPercentage(BigDecimal a, BigDecimal b) {
        if (a == null || b == null || b.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(100);
        BigDecimal diff = a.subtract(b).abs();
        return diff.divide(b, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
