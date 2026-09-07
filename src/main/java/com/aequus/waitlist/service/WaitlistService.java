package com.aequus.waitlist.service;

import com.aequus.waitlist.dto.WaitlistRequest;
import com.aequus.waitlist.dto.WaitlistResponse;
import com.aequus.waitlist.entity.Waitlist;
import com.aequus.waitlist.repository.WaitlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    public WaitlistService(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @Transactional
    public WaitlistResponse joinWaitlist(WaitlistRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Optional<Waitlist> existing = waitlistRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            Waitlist waitlist = existing.get();
            return new WaitlistResponse(
                    waitlist.getId(),
                    waitlist.getEmail(),
                    "Priority clearance already registered. Hardware enclave key queued.",
                    waitlist.getCreatedAt()
            );
        }

        Waitlist saved = waitlistRepository.save(new Waitlist(normalizedEmail));
        return new WaitlistResponse(
                saved.getId(),
                saved.getEmail(),
                "Priority clearance confirmed. Check your inbox for the hardware enclave attestation key.",
                saved.getCreatedAt()
        );
    }
}
