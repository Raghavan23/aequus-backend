package com.aequus.organization.controller;

import com.aequus.organization.dto.OrganizationDtos.OrganizationRequest;
import com.aequus.organization.dto.OrganizationDtos.OrganizationResponse;
import com.aequus.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse response = organizationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(@PathVariable UUID id, @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }
}
