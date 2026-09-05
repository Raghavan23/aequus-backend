package com.aequus.financial.service;

import com.aequus.account.entity.Account;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.dto.FinancialRecordRequest;
import com.aequus.financial.dto.FinancialRecordResponse;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.repository.FinancialRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final AccountRepository accountRepository;
    private final CurrentUserProvider currentUserProvider;

    public FinancialRecordService(FinancialRecordRepository financialRecordRepository,
                                  AccountRepository accountRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.financialRecordRepository = financialRecordRepository;
        this.accountRepository = accountRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public FinancialRecordResponse create(FinancialRecordRequest request) {
        request.category().validateBelongsTo(request.type());

        UUID userId = currentUserProvider.getCurrentUserId();
        String accountName = null;

        if (request.accountId() != null) {
            Account account = getOwnedAccountOrThrow(request.accountId(), userId);
            accountName = account.getName();
            applyBalanceChange(account, request.type(), request.amount());
        }

        FinancialRecord record = new FinancialRecord(
                userId,
                request.accountId(),
                request.type(),
                request.category(),
                request.amount()
        );
        FinancialRecord saved = financialRecordRepository.save(record);

        return FinancialRecordResponse.from(saved, accountName);
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getAllForCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        Map<UUID, String> accountNames = accountRepository.findAllByUserIdOrderByCreatedAtAsc(userId).stream()
                .collect(Collectors.toMap(Account::getId, Account::getName, (existing, replacement) -> existing));

        return financialRecordRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(record -> FinancialRecordResponse.from(
                        record,
                        record.getAccountId() != null ? accountNames.get(record.getAccountId()) : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getById(UUID id) {
        FinancialRecord record = getOwnedRecordOrThrow(id);
        String accountName = null;
        if (record.getAccountId() != null) {
            accountName = accountRepository.findByIdAndUserId(record.getAccountId(), record.getUserId())
                    .map(Account::getName)
                    .orElse(null);
        }
        return FinancialRecordResponse.from(record, accountName);
    }

    @Transactional
    public FinancialRecordResponse update(UUID id, FinancialRecordRequest request) {
        request.category().validateBelongsTo(request.type());

        UUID userId = currentUserProvider.getCurrentUserId();
        FinancialRecord record = getOwnedRecordOrThrow(id);

        // 1. Revert previous balance impact on old account (if any)
        if (record.getAccountId() != null) {
            accountRepository.findByIdAndUserId(record.getAccountId(), userId).ifPresent(oldAccount ->
                    revertBalanceChange(oldAccount, record.getType(), record.getAmount())
            );
        }

        // 2. Apply new balance impact on target account (if any)
        String accountName = null;
        if (request.accountId() != null) {
            Account newAccount = getOwnedAccountOrThrow(request.accountId(), userId);
            accountName = newAccount.getName();
            applyBalanceChange(newAccount, request.type(), request.amount());
        }

        // 3. Update record fields
        record.update(request.accountId(), request.type(), request.category(), request.amount());

        return FinancialRecordResponse.from(record, accountName);
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        FinancialRecord record = getOwnedRecordOrThrow(id);

        // Revert balance impact on account before deleting
        if (record.getAccountId() != null) {
            accountRepository.findByIdAndUserId(record.getAccountId(), userId).ifPresent(account ->
                    revertBalanceChange(account, record.getType(), record.getAmount())
            );
        }

        financialRecordRepository.deleteByIdAndUserId(id, userId);
    }

    private void applyBalanceChange(Account account, FinancialType type, BigDecimal amount) {
        if (type == FinancialType.INCOME) {
            account.credit(amount);
        } else if (type == FinancialType.EXPENSE) {
            account.debit(amount);
        }
    }

    private void revertBalanceChange(Account account, FinancialType type, BigDecimal amount) {
        if (type == FinancialType.INCOME) {
            account.debit(amount);
        } else if (type == FinancialType.EXPENSE) {
            account.credit(amount);
        }
    }

    private Account getOwnedAccountOrThrow(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private FinancialRecord getOwnedRecordOrThrow(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return financialRecordRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found"));
    }
}
