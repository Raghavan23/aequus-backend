package com.aequus.client.repository;

import com.aequus.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<Client> findAllByOrganizationIdAndActiveTrueOrderByNameAsc(UUID organizationId);

    Optional<Client> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
