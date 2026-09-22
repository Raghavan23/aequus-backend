package com.aequus.financial.statement.service;

import com.aequus.account.entity.Account;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.repository.FinancialRecordRepository;
import com.aequus.financial.statement.dto.StatementDtos.*;
import com.aequus.financial.statement.exception.StatementParseException;
import com.aequus.financial.statement.parser.CsvStatementParser;
import com.aequus.financial.statement.parser.PdfStatementParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class StatementImportService {

    private final CsvStatementParser csvStatementParser;
    private final PdfStatementParser pdfStatementParser;
    private final AccountRepository accountRepository;
    private final FinancialRecordRepository financialRecordRepository;
    private final CurrentUserProvider currentUserProvider;

    public StatementImportService(
            CsvStatementParser csvStatementParser,
            PdfStatementParser pdfStatementParser,
            AccountRepository accountRepository,
            FinancialRecordRepository financialRecordRepository,
            CurrentUserProvider currentUserProvider) {
        this.csvStatementParser = csvStatementParser;
        this.pdfStatementParser = pdfStatementParser;
        this.accountRepository = accountRepository;
        this.financialRecordRepository = financialRecordRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public StatementParseResponse parseStatement(MultipartFile file, UUID accountId) {
        validateFileNotEmpty(file);
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Account targetAccount = getOwnedAccountOrThrow(accountId, currentUserId);

        List<ParsedStatementItem> rawParsedItems = parseRawFile(file);
        List<FinancialRecord> existingRecords = financialRecordRepository
                .findAllByUserIdAndAccountIdAndIsDeletedFalse(currentUserId, accountId);

        List<ParsedStatementItem> itemsWithDuplicatesFlagged = flagDuplicates(rawParsedItems, existingRecords);

        return buildParseResponse(file.getOriginalFilename(), targetAccount, itemsWithDuplicatesFlagged);
    }

    @Transactional
    public StatementImportResultResponse confirmImport(ConfirmStatementImportRequest request) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        Account targetAccount = getOwnedAccountOrThrow(request.accountId(), currentUserId);

        List<FinancialRecord> recordsToSave = new ArrayList<>();
        BigDecimal totalBalanceDelta = BigDecimal.ZERO;

        for (ImportItemRequest item : request.items()) {
            UUID effectiveAccountId = targetAccount.getId() != null ? targetAccount.getId() : request.accountId();
            FinancialRecord record = new FinancialRecord(
                    currentUserId,
                    effectiveAccountId,
                    item.type(),
                    item.category(),
                    item.amount()
            );
            recordsToSave.add(record);

            if (item.type() == FinancialType.INCOME) {
                targetAccount.credit(item.amount());
                totalBalanceDelta = totalBalanceDelta.add(item.amount());
            } else {
                targetAccount.debit(item.amount());
                totalBalanceDelta = totalBalanceDelta.subtract(item.amount());
            }
        }

        financialRecordRepository.saveAll(recordsToSave);
        accountRepository.save(targetAccount);

        return new StatementImportResultResponse(
                targetAccount.getId(),
                recordsToSave.size(),
                targetAccount.getBalance(),
                String.format("Successfully imported %d transactions into %s.", recordsToSave.size(), targetAccount.getName())
        );
    }

    private List<ParsedStatementItem> parseRawFile(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        try (InputStream inputStream = file.getInputStream()) {
            if (filename.endsWith(".pdf") || "application/pdf".equalsIgnoreCase(file.getContentType())) {
                return pdfStatementParser.parse(inputStream);
            }
            return csvStatementParser.parse(inputStream);
        } catch (IOException e) {
            throw new StatementParseException("Could not read uploaded statement file.", e);
        }
    }

    private List<ParsedStatementItem> flagDuplicates(
            List<ParsedStatementItem> parsedItems,
            List<FinancialRecord> existingRecords) {
        return parsedItems.stream()
                .map(item -> isDuplicateRecord(item, existingRecords)
                        ? withDuplicateFlag(item, true)
                        : item)
                .toList();
    }

    private boolean isDuplicateRecord(ParsedStatementItem item, List<FinancialRecord> existingRecords) {
        return existingRecords.stream().anyMatch(record ->
                record.getType() == item.type()
                        && record.getAmount().compareTo(item.amount()) == 0
                        && record.getCategory() == item.category()
        );
    }

    private ParsedStatementItem withDuplicateFlag(ParsedStatementItem item, boolean isDuplicate) {
        return new ParsedStatementItem(
                item.id(),
                item.date(),
                item.description(),
                item.type(),
                item.category(),
                item.amount(),
                isDuplicate,
                item.rawLine()
        );
    }

    private StatementParseResponse buildParseResponse(
            String filename,
            Account account,
            List<ParsedStatementItem> items) {
        int duplicateCount = (int) items.stream().filter(ParsedStatementItem::isDuplicate).count();
        int newCount = items.size() - duplicateCount;

        BigDecimal totalIncome = items.stream()
                .filter(i -> i.type() == FinancialType.INCOME)
                .map(ParsedStatementItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = items.stream()
                .filter(i -> i.type() == FinancialType.EXPENSE)
                .map(ParsedStatementItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatementParseResponse(
                filename,
                account.getId(),
                account.getName(),
                items.size(),
                newCount,
                duplicateCount,
                totalIncome,
                totalExpense,
                items
        );
    }

    private Account getOwnedAccountOrThrow(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StatementParseException("Uploaded statement file cannot be empty.");
        }
    }
}
