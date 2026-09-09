package com.aequus.ai.vision.repository;

import com.aequus.ai.vision.entity.ReceiptScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptScanRepository extends JpaRepository<ReceiptScan, UUID> {

    List<ReceiptScan> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<ReceiptScan> findByIdAndUserId(UUID id, UUID userId);
}
