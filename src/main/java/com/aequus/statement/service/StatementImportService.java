package com.aequus.statement.service;

import com.aequus.audit.service.AuditService;
import com.aequus.client.entity.Client;
import com.aequus.client.repository.ClientRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.organization.entity.Organization;
import com.aequus.statement.dto.StatementDtos.*;
import com.aequus.statement.parser.BankStatementParser;
import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.TransactionType;
import com.aequus.transaction.repository.BankTransactionRepository;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StatementImportService {

    private final BankStatementParser bankStatementParser;
    private final BankTransactionRepository bankTransactionRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public StatementImportService(BankStatementParser bankStatementParser,
                                  BankTransactionRepository bankTransactionRepository,
                                  ClientRepository clientRepository,
                                  UserRepository userRepository,
                                  CurrentUserProvider currentUserProvider,
                                  AuditService auditService) {
        this.bankStatementParser = bankStatementParser;
        this.bankTransactionRepository = bankTransactionRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    private User getCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Organization getOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("User does not belong to any organization");
        }
        return user.getOrganization();
    }

    @Transactional(readOnly = true)
    public BankStatementParseResponse parseStatement(UUID clientId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded statement file cannot be empty");
        }

        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Client client = clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "statement.csv";
        List<ParsedBankStatementItem> parsedItems;

        try {
            if (filename.toLowerCase().endsWith(".pdf")) {
                parsedItems = bankStatementParser.parsePdf(file.getInputStream());
            } else {
                parsedItems = bankStatementParser.parseCsv(file.getInputStream());
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse statement: " + e.getMessage());
        }

        int duplicates = 0;
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<ParsedBankStatementItem> flaggedItems = new ArrayList<>();

        for (ParsedBankStatementItem item : parsedItems) {
            long dupCount = bankTransactionRepository.countDuplicates(clientId, item.date(), item.amount(), item.narration());
            boolean isDup = dupCount > 0;
            if (isDup) duplicates++;

            if (item.type() == TransactionType.DEBIT) {
                totalDebit = totalDebit.add(item.amount());
            } else {
                totalCredit = totalCredit.add(item.amount());
            }

            flaggedItems.add(new ParsedBankStatementItem(
                    item.id(),
                    item.date(),
                    item.narration(),
                    item.referenceNumber(),
                    item.type(),
                    item.accountingHead(),
                    item.amount(),
                    item.balanceAfter(),
                    isDup,
                    item.rawLine()
            ));
        }

        return new BankStatementParseResponse(
                filename,
                client.getId(),
                client.getName(),
                flaggedItems.size(),
                flaggedItems.size() - duplicates,
                duplicates,
                totalDebit,
                totalCredit,
                flaggedItems
        );
    }

    @Transactional
    public BankStatementImportResultResponse confirmImport(ConfirmBankStatementImportRequest request) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Client client = clientRepository.findByIdAndOrganizationId(request.clientId(), org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        List<BankTransaction> toSave = new ArrayList<>();
        String sourceFile = (request.sourceFilename() != null && !request.sourceFilename().isBlank())
                ? request.sourceFilename()
                : "Statement Import";

        for (BankImportItemRequest item : request.items()) {
            BankTransaction tx = new BankTransaction(
                    org,
                    client,
                    item.date(),
                    item.narration(),
                    item.referenceNumber(),
                    item.type(),
                    item.amount(),
                    item.balanceAfter(),
                    item.accountingHead() != null ? item.accountingHead() : AccountingHead.MISC,
                    sourceFile,
                    item.rawLine()
            );
            toSave.add(tx);
        }

        bankTransactionRepository.saveAll(toSave);

        auditService.log(
                org,
                user,
                "STATEMENT_IMPORTED",
                "CLIENT",
                client.getId(),
                String.format("Imported %d transactions from file '%s' for client '%s'", toSave.size(), sourceFile, client.getName())
        );

        return new BankStatementImportResultResponse(
                client.getId(),
                toSave.size(),
                String.format("Successfully imported %d transactions for %s.", toSave.size(), client.getName())
        );
    }
}
