package com.aequus.audit.repository;

import com.aequus.audit.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {

    List<AuditEntry> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<AuditEntry> findTop50ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
