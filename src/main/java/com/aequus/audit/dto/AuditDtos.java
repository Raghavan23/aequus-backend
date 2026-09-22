package com.aequus.audit.dto;

import com.aequus.audit.entity.AuditEntry;

import java.time.Instant;
import java.util.UUID;

public class AuditDtos {

    public record AuditEntryResponse(
            UUID id,
            UUID organizationId,
            UUID userId,
            String userName,
            String userEmail,
            String action,
            String entityType,
            UUID entityId,
            String details,
            Instant createdAt
    ) {
        public static AuditEntryResponse from(AuditEntry entry) {
            return new AuditEntryResponse(
                    entry.getId(),
                    entry.getOrganization().getId(),
                    entry.getUser().getId(),
                    entry.getUser().getName(),
                    entry.getUser().getEmail(),
                    entry.getAction(),
                    entry.getEntityType(),
                    entry.getEntityId(),
                    entry.getDetails(),
                    entry.getCreatedAt()
            );
        }
    }
}
