package com.aequus.common.security;

import com.aequus.common.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the currently authenticated user's id from the security context.
 * Services use this instead of trusting any client-supplied user id.
 */
@Component
public class CurrentUserProvider {

    public UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return authenticatedUser.id();
    }
}
