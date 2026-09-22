package com.aequus.audit.service;

import com.aequus.audit.dto.AuditDtos.AuditEntryResponse;
import com.aequus.audit.entity.AuditEntry;
import com.aequus.audit.repository.AuditRepository;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.organization.entity.Organization;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public AuditService(AuditRepository auditRepository,
                        UserRepository userRepository,
                        CurrentUserProvider currentUserProvider) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void log(Organization organization, User user, String action, String entityType, UUID entityId, String details) {
        AuditEntry entry = new AuditEntry(organization, user, action, entityType, entityId, details);
        auditRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> getRecentAuditLogs() {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getOrganization() == null) {
            return List.of();
        }

        return auditRepository.findTop50ByOrganizationIdOrderByCreatedAtDesc(user.getOrganization().getId())
                .stream()
                .map(AuditEntryResponse::from)
                .toList();
    }
}
