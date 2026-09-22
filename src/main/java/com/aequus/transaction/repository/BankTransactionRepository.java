package com.aequus.transaction.repository;

import com.aequus.transaction.entity.BankTransaction;
import com.aequus.transaction.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    List<BankTransaction> findAllByClientIdOrderByTransactionDateDesc(UUID clientId);

    List<BankTransaction> findAllByClientIdAndMatchStatusOrderByTransactionDateDesc(UUID clientId, MatchStatus matchStatus);

    List<BankTransaction> findAllByClientIdAndTransactionDateBetweenOrderByTransactionDateAsc(UUID clientId, LocalDate startDate, LocalDate endDate);

    Optional<BankTransaction> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByClientId(UUID clientId);

    long countByClientIdAndMatchStatus(UUID clientId, MatchStatus matchStatus);

    long countByOrganizationId(UUID organizationId);

    long countByOrganizationIdAndMatchStatus(UUID organizationId, MatchStatus matchStatus);

    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.client.id = :clientId AND t.transactionDate = :date AND t.amount = :amount AND t.narration = :narration")
    long countDuplicates(@Param("clientId") UUID clientId,
                        @Param("date") LocalDate date,
                        @Param("amount") java.math.BigDecimal amount,
                        @Param("narration") String narration);
}
