package com.aequus.transaction.service;

import com.aequus.client.entity.Client;
import com.aequus.client.repository.ClientRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.organization.entity.Organization;
import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionRequest;
import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionResponse;
import com.aequus.transaction.dto.BankTransactionDtos.TransactionSummaryResponse;
import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.MatchStatus;
import com.aequus.transaction.entity.TransactionType;
import com.aequus.transaction.repository.BankTransactionRepository;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class BankTransactionService {

    private final BankTransactionRepository bankTransactionRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public BankTransactionService(BankTransactionRepository bankTransactionRepository,
                                  ClientRepository clientRepository,
                                  UserRepository userRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
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

    @Transactional
    public BankTransactionResponse create(BankTransactionRequest request) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Client client = clientRepository.findByIdAndOrganizationId(request.clientId(), org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        BankTransaction tx = new BankTransaction(
                org,
                client,
                request.transactionDate(),
                request.narration(),
                request.referenceNumber(),
                request.type(),
                request.amount(),
                request.balanceAfter(),
                request.accountingHead(),
                "MANUAL",
                request.narration()
        );

        BankTransaction saved = bankTransactionRepository.save(tx);
        return BankTransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BankTransactionResponse> getTransactionsByClient(UUID clientId, MatchStatus status) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        // Verify client belongs to org
        clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<BankTransaction> list = (status != null)
                ? bankTransactionRepository.findAllByClientIdAndMatchStatusOrderByTransactionDateDesc(clientId, status)
                : bankTransactionRepository.findAllByClientIdOrderByTransactionDateDesc(clientId);

        return list.stream().map(BankTransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionSummaryResponse getClientSummary(UUID clientId) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<BankTransaction> all = bankTransactionRepository.findAllByClientIdOrderByTransactionDateDesc(clientId);

        long total = all.size();
        long matched = all.stream().filter(t -> t.getMatchStatus() == MatchStatus.MATCHED).count();
        long anomalies = all.stream().filter(t -> t.getMatchStatus() == MatchStatus.ANOMALY).count();
        long unmatched = all.stream().filter(t -> t.getMatchStatus() == MatchStatus.UNMATCHED).count();

        BigDecimal debitVol = all.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditVol = all.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double matchRate = total > 0
                ? BigDecimal.valueOf((double) matched / total * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new TransactionSummaryResponse(total, matched, anomalies, unmatched, debitVol, creditVol, matchRate);
    }

    @Transactional
    public BankTransactionResponse updateAccountingHead(UUID id, AccountingHead head) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        BankTransaction tx = bankTransactionRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction", id));

        tx.setAccountingHead(head);
        BankTransaction saved = bankTransactionRepository.save(tx);
        return BankTransactionResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        BankTransaction tx = bankTransactionRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction", id));

        bankTransactionRepository.delete(tx);
    }
}
