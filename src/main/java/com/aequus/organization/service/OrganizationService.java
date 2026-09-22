package com.aequus.organization.service;

import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.organization.dto.OrganizationDtos.OrganizationRequest;
import com.aequus.organization.dto.OrganizationDtos.OrganizationResponse;
import com.aequus.organization.entity.Organization;
import com.aequus.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public Organization createDefaultOrganization(String name) {
        Organization org = new Organization(name);
        return organizationRepository.save(org);
    }

    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        Organization org = new Organization(
                request.name(),
                request.gstin(),
                request.pan(),
                request.address(),
                request.phone(),
                "FREE"
        );
        Organization saved = organizationRepository.save(org);
        return OrganizationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getById(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
        return OrganizationResponse.from(org);
    }

    @Transactional
    public OrganizationResponse update(UUID id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));

        org.setName(request.name());
        org.setGstin(request.gstin());
        org.setPan(request.pan());
        org.setAddress(request.address());
        org.setPhone(request.phone());

        Organization updated = organizationRepository.save(org);
        return OrganizationResponse.from(updated);
    }
}
