package com.finz.user.dto;

import com.finz.user.entity.User;

import java.time.Instant;
import java.util.UUID;

/**
 * API-facing user representation. Never exposes the password hash.
 */
public record UserResponse(
        UUID id,
        String name,
        String email,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
