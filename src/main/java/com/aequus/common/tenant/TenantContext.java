package com.aequus.common.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORGANIZATION = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setOrganizationId(UUID organizationId) {
        CURRENT_ORGANIZATION.set(organizationId);
    }

    public static UUID getOrganizationId() {
        return CURRENT_ORGANIZATION.get();
    }

    public static void clear() {
        CURRENT_ORGANIZATION.remove();
    }
}
