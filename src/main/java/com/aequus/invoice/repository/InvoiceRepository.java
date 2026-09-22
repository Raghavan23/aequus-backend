package com.aequus.invoice.repository;

import com.aequus.invoice.entity.Invoice;
import com.aequus.transaction.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByClientIdOrderByInvoiceDateDesc(UUID clientId);

    List<Invoice> findAllByClientIdAndMatchStatusOrderByInvoiceDateDesc(UUID clientId, MatchStatus matchStatus);

    List<Invoice> findAllByClientIdAndInvoiceDateBetweenOrderByInvoiceDateAsc(UUID clientId, LocalDate startDate, LocalDate endDate);

    Optional<Invoice> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByClientId(UUID clientId);

    long countByClientIdAndMatchStatus(UUID clientId, MatchStatus matchStatus);

    long countByOrganizationId(UUID organizationId);
}
