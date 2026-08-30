package com.finz.financial.repository;

import com.finz.financial.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    List<FinancialRecord> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<FinancialRecord> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
