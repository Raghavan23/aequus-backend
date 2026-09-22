package com.aequus.reconciliation.repository;

import com.aequus.reconciliation.entity.ReconciliationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReconciliationJobRepository extends JpaRepository<ReconciliationJob, UUID> {

    List<ReconciliationJob> findAllByClientIdOrderByCreatedAtDesc(UUID clientId);

    Optional<ReconciliationJob> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<ReconciliationJob> findFirstByClientIdOrderByCreatedAtDesc(UUID clientId);
}
