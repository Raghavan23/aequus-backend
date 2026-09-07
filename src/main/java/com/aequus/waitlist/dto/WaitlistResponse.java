package com.aequus.waitlist.dto;

import java.time.Instant;
import java.util.UUID;

public class WaitlistResponse {

    private UUID id;
    private String email;
    private String message;
    private Instant createdAt;

    public WaitlistResponse() {
    }

    public WaitlistResponse(UUID id, String email, String message, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.message = message;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
