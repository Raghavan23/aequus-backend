package com.aequus.common.security;

import java.util.UUID;

/**
 * Lightweight representation of the currently authenticated user, used as the
 * Spring Security principal. Keeping this independent of the JPA entity avoids
 * a dependency from common -> user module.
 */
public record AuthenticatedUser(UUID id, String email) {
}
