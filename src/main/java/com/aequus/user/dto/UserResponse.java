package com.aequus.user.dto;

import com.aequus.user.entity.User;
import com.aequus.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * API-facing user representation. Never exposes the password hash.
 */
public record UserResponse(
        UUID id,
        String name,
        String email,
        UUID organizationId,
        String organizationName,
        UserRole role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        UUID orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        String orgName = user.getOrganization() != null ? user.getOrganization().getName() : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                orgId,
                orgName,
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
