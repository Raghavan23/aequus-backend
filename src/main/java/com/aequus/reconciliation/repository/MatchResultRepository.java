package com.aequus.reconciliation.repository;

import com.aequus.reconciliation.entity.MatchResult;
import com.aequus.reconciliation.entity.MatchResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findAllByJobIdOrderByConfidenceScoreDesc(UUID jobId);

    List<MatchResult> findAllByJobIdAndStatus(UUID jobId, MatchResultStatus status);

    Optional<MatchResult> findByTransactionId(UUID transactionId);

    void deleteAllByJobId(UUID jobId);
}
