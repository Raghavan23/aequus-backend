package com.aequus.financial.repository;

import com.aequus.financial.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    List<FinancialRecord> findAllByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<FinancialRecord> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    Optional<FinancialRecord> findByIdAndUserId(UUID id, UUID userId);
}
