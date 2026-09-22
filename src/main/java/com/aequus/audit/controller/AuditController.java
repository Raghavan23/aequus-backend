package com.aequus.audit.controller;

import com.aequus.audit.dto.AuditDtos.AuditEntryResponse;
import com.aequus.audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditEntryResponse>> getRecentLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs());
    }
}
