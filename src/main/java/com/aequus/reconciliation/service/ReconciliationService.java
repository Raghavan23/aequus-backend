package com.aequus.reconciliation.service;

import com.aequus.audit.service.AuditService;
import com.aequus.client.entity.Client;
import com.aequus.client.repository.ClientRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.invoice.dto.InvoiceDtos.InvoiceResponse;
import com.aequus.invoice.entity.Invoice;
import com.aequus.invoice.repository.InvoiceRepository;
import com.aequus.organization.entity.Organization;
import com.aequus.reconciliation.dto.ReconciliationDtos.*;
import com.aequus.reconciliation.engine.DeterministicMatcher;
import com.aequus.reconciliation.engine.DeterministicMatcher.MatchCandidate;
import com.aequus.reconciliation.entity.MatchResult;
import com.aequus.reconciliation.entity.MatchResultStatus;
import com.aequus.reconciliation.entity.ReconciliationJob;
import com.aequus.reconciliation.entity.ReconciliationJobStatus;
import com.aequus.reconciliation.repository.MatchResultRepository;
import com.aequus.reconciliation.repository.ReconciliationJobRepository;
import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionResponse;
import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.MatchStatus;
import com.aequus.transaction.repository.BankTransactionRepository;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ReconciliationService {

    private final ReconciliationJobRepository jobRepository;
    private final MatchResultRepository matchResultRepository;
    private final BankTransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final DeterministicMatcher deterministicMatcher;
    private final AuditService auditService;

    public ReconciliationService(ReconciliationJobRepository jobRepository,
                                 MatchResultRepository matchResultRepository,
                                 BankTransactionRepository transactionRepository,
                                 InvoiceRepository invoiceRepository,
                                 ClientRepository clientRepository,
                                 UserRepository userRepository,
                                 CurrentUserProvider currentUserProvider,
                                 DeterministicMatcher deterministicMatcher,
                                 AuditService auditService) {
        this.jobRepository = jobRepository;
        this.matchResultRepository = matchResultRepository;
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.deterministicMatcher = deterministicMatcher;
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

    @Transactional
    public ReconciliationJobResponse runReconciliation(UUID clientId) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Client client = clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<BankTransaction> transactions = transactionRepository.findAllByClientIdOrderByTransactionDateDesc(clientId);
        List<Invoice> invoices = invoiceRepository.findAllByClientIdOrderByInvoiceDateDesc(clientId);

        ReconciliationJob job = new ReconciliationJob(org, client);
        job.setStatus(ReconciliationJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        job.setTotalTransactions(transactions.size());
        ReconciliationJob savedJob = jobRepository.save(job);

        List<MatchCandidate> candidates = deterministicMatcher.findMatches(transactions, invoices);

        Set<UUID> matchedTxnIds = new HashSet<>();
        Set<UUID> matchedInvIds = new HashSet<>();
        int autoMatchedCount = 0;

        for (MatchCandidate candidate : candidates) {
            BankTransaction txn = candidate.transaction();
            Invoice inv = candidate.invoice();

            MatchResultStatus status = candidate.autoAccept() ? MatchResultStatus.ACCEPTED : MatchResultStatus.SUGGESTED;

            MatchResult matchResult = new MatchResult(
                    savedJob,
                    txn,
                    inv,
                    candidate.matchType(),
                    candidate.confidenceScore(),
                    candidate.reasoning(),
                    status
            );

            if (candidate.autoAccept()) {
                matchResult.setReviewedBy(user);
                matchResult.setReviewedAt(Instant.now());

                txn.setMatchStatus(MatchStatus.MATCHED);
                txn.setMatchedInvoiceId(inv.getId());
                transactionRepository.save(txn);

                inv.setMatchStatus(MatchStatus.MATCHED);
                inv.setMatchedTxnId(txn.getId());
                invoiceRepository.save(inv);

                autoMatchedCount++;
            }

            matchResultRepository.save(matchResult);
            matchedTxnIds.add(txn.getId());
            matchedInvIds.add(inv.getId());
        }

        // Detect anomalies and mark unmatched transactions
        int anomalyCount = 0;
        int unmatchedCount = 0;

        for (BankTransaction txn : transactions) {
            if (!matchedTxnIds.contains(txn.getId())) {
                // If it's a debit without invoice, flag as anomaly if high amount or unmatched
                if (txn.getAmount().doubleValue() > 5000) {
                    txn.setMatchStatus(MatchStatus.ANOMALY);
                    anomalyCount++;
                } else {
                    txn.setMatchStatus(MatchStatus.UNMATCHED);
                    unmatchedCount++;
                }
                transactionRepository.save(txn);
            }
        }

        savedJob.setMatchedCount(autoMatchedCount);
        savedJob.setAnomalyCount(anomalyCount);
        savedJob.setUnmatchedCount(unmatchedCount);
        savedJob.setStatus(ReconciliationJobStatus.COMPLETED);
        savedJob.setCompletedAt(Instant.now());
        ReconciliationJob completedJob = jobRepository.save(savedJob);

        auditService.log(
                org,
                user,
                "RECONCILIATION_RUN",
                "RECONCILIATION_JOB",
                completedJob.getId(),
                String.format("Reconciliation ran for client '%s'. Total: %d, Matched: %d, Anomalies: %d, Unmatched: %d",
                        client.getName(), completedJob.getTotalTransactions(), autoMatchedCount, anomalyCount, unmatchedCount)
        );

        return ReconciliationJobResponse.from(completedJob);
    }

    @Transactional
    public MatchResultResponse reviewMatch(UUID matchResultId, MatchResultStatus action) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        MatchResult matchResult = matchResultRepository.findById(matchResultId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultId));

        if (!matchResult.getJob().getOrganization().getId().equals(org.getId())) {
            throw new BadRequestException("Unauthorized access to match result");
        }

        matchResult.setStatus(action);
        matchResult.setReviewedBy(user);
        matchResult.setReviewedAt(Instant.now());

        BankTransaction txn = matchResult.getTransaction();
        Invoice inv = matchResult.getInvoice();

        if (action == MatchResultStatus.ACCEPTED) {
            if (txn != null) {
                txn.setMatchStatus(MatchStatus.MATCHED);
                if (inv != null) txn.setMatchedInvoiceId(inv.getId());
                transactionRepository.save(txn);
            }
            if (inv != null) {
                inv.setMatchStatus(MatchStatus.MATCHED);
                if (txn != null) inv.setMatchedTxnId(txn.getId());
                invoiceRepository.save(inv);
            }
        } else if (action == MatchResultStatus.REJECTED) {
            if (txn != null) {
                txn.setMatchStatus(MatchStatus.UNMATCHED);
                txn.setMatchedInvoiceId(null);
                transactionRepository.save(txn);
            }
            if (inv != null) {
                inv.setMatchStatus(MatchStatus.UNMATCHED);
                inv.setMatchedTxnId(null);
                invoiceRepository.save(inv);
            }
        }

        MatchResult saved = matchResultRepository.save(matchResult);

        auditService.log(
                org,
                user,
                "MATCH_" + action.name(),
                "MATCH_RESULT",
                saved.getId(),
                String.format("Match result %s by %s for Txn #%s and Inv #%s",
                        action.name(), user.getName(), txn != null ? txn.getId() : "N/A", inv != null ? inv.getInvoiceNumber() : "N/A")
        );

        return MatchResultResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReconciliationWorkspaceResponse getWorkspace(UUID clientId) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        ReconciliationJob latestJob = jobRepository.findFirstByClientIdOrderByCreatedAtDesc(clientId).orElse(null);

        List<MatchResultResponse> matches = (latestJob != null)
                ? matchResultRepository.findAllByJobIdOrderByConfidenceScoreDesc(latestJob.getId())
                    .stream().map(MatchResultResponse::from).toList()
                : List.of();

        List<BankTransactionResponse> unmatchedTxns = transactionRepository
                .findAllByClientIdOrderByTransactionDateDesc(clientId)
                .stream()
                .filter(t -> t.getMatchStatus() == MatchStatus.UNMATCHED || t.getMatchStatus() == MatchStatus.ANOMALY)
                .map(BankTransactionResponse::from)
                .toList();

        List<InvoiceResponse> unmatchedInvoices = invoiceRepository
                .findAllByClientIdOrderByInvoiceDateDesc(clientId)
                .stream()
                .filter(i -> i.getMatchStatus() == MatchStatus.UNMATCHED)
                .map(InvoiceResponse::from)
                .toList();

        return new ReconciliationWorkspaceResponse(
                latestJob != null ? ReconciliationJobResponse.from(latestJob) : null,
                matches,
                unmatchedTxns,
                unmatchedInvoices
        );
    }
}
