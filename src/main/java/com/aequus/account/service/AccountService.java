package com.aequus.account.service;

import com.aequus.account.dto.AccountRequest;
import com.aequus.account.dto.AccountResponse;
import com.aequus.account.dto.AccountSummaryResponse;
import com.aequus.account.entity.Account;
import com.aequus.account.repository.AccountRepository;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrentUserProvider currentUserProvider;

    public AccountService(AccountRepository accountRepository, CurrentUserProvider currentUserProvider) {
        this.accountRepository = accountRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Account account = new Account(
                userId,
                request.name().trim(),
                request.type(),
                request.currency(),
                request.balance() != null ? request.balance() : BigDecimal.ZERO,
                request.institutionName() != null ? request.institutionName().trim() : null,
                request.accountNumberMask() != null ? request.accountNumberMask().trim() : null,
                request.color(),
                request.icon()
        );
        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllForCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return accountRepository.findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(userId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        return AccountResponse.from(getOwnedAccountOrThrow(id));
    }

    @Transactional
    public AccountResponse update(UUID id, AccountRequest request) {
        Account account = getOwnedAccountOrThrow(id);
        account.update(
                request.name().trim(),
                request.type(),
                request.currency(),
                request.balance(),
                request.institutionName() != null ? request.institutionName().trim() : null,
                request.accountNumberMask() != null ? request.accountNumberMask().trim() : null,
                request.color(),
                request.icon()
        );
        return AccountResponse.from(account);
    }

    @Transactional
    public void archive(UUID id) {
        Account account = getOwnedAccountOrThrow(id);
        account.setArchived(true);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = getOwnedAccountOrThrow(id);
        account.softDelete();
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public AccountSummaryResponse getSummary() {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<Account> accounts = accountRepository.findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(userId);

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;

        for (Account acc : accounts) {
            if (acc.getType().isLiability()) {
                totalLiabilities = totalLiabilities.add(acc.getBalance().abs());
            } else {
                totalAssets = totalAssets.add(acc.getBalance());
            }
        }

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        return new AccountSummaryResponse(
                totalAssets,
                totalLiabilities,
                netWorth,
                accounts.size()
        );
    }

    @Transactional
    public void creditAccount(UUID accountId, UUID userId, BigDecimal amount) {
        if (accountId == null) return;
        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for transaction"));
        account.credit(amount);
    }

    @Transactional
    public void debitAccount(UUID accountId, UUID userId, BigDecimal amount) {
        if (accountId == null) return;
        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for transaction"));
        account.debit(amount);
    }

    public Account getOwnedAccountOrThrow(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return accountRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
