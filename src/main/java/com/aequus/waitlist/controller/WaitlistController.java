package com.aequus.waitlist.controller;

import com.aequus.waitlist.dto.WaitlistRequest;
import com.aequus.waitlist.dto.WaitlistResponse;
import com.aequus.waitlist.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        WaitlistResponse response = waitlistService.joinWaitlist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
