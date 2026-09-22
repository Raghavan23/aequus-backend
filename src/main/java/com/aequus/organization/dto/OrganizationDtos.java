package com.aequus.organization.dto;

import com.aequus.organization.entity.Organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class OrganizationDtos {

    public record OrganizationRequest(
            @NotBlank(message = "Organization name is required")
            @Size(max = 200, message = "Name must not exceed 200 characters")
            String name,

            @Size(max = 15, message = "GSTIN must not exceed 15 characters")
            String gstin,

            @Size(max = 10, message = "PAN must not exceed 10 characters")
            String pan,

            String address,
            String phone
    ) {}

    public record OrganizationResponse(
            UUID id,
            String name,
            String gstin,
            String pan,
            String address,
            String phone,
            String planTier,
            Instant createdAt
    ) {
        public static OrganizationResponse from(Organization org) {
            return new OrganizationResponse(
                    org.getId(),
                    org.getName(),
                    org.getGstin(),
                    org.getPan(),
                    org.getAddress(),
                    org.getPhone(),
                    org.getPlanTier(),
                    org.getCreatedAt()
            );
        }
    }
}
