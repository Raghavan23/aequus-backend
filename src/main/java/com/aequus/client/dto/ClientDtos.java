package com.aequus.client.dto;

import com.aequus.client.entity.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class ClientDtos {

    public record ClientRequest(
            @NotBlank(message = "Client company name is required")
            @Size(max = 200, message = "Name must not exceed 200 characters")
            String name,

            @Size(max = 15, message = "GSTIN must not exceed 15 characters")
            String gstin,

            @Size(max = 10, message = "PAN must not exceed 10 characters")
            String pan,

            @Size(max = 200, message = "Tally company name must not exceed 200 characters")
            String tallyCompanyName,

            @Size(max = 150, message = "Contact person must not exceed 150 characters")
            String contactPerson,

            String contactEmail,
            String contactPhone
    ) {}

    public record ClientResponse(
            UUID id,
            UUID organizationId,
            String name,
            String gstin,
            String pan,
            String tallyCompanyName,
            String contactPerson,
            String contactEmail,
            String contactPhone,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ClientResponse from(Client client) {
            return new ClientResponse(
                    client.getId(),
                    client.getOrganization().getId(),
                    client.getName(),
                    client.getGstin(),
                    client.getPan(),
                    client.getTallyCompanyName(),
                    client.getContactPerson(),
                    client.getContactEmail(),
                    client.getContactPhone(),
                    client.isActive(),
                    client.getCreatedAt(),
                    client.getUpdatedAt()
            );
        }
    }
}
