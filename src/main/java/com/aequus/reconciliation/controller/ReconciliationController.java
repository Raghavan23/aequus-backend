package com.aequus.reconciliation.controller;

import com.aequus.reconciliation.dto.ReconciliationDtos.*;
import com.aequus.reconciliation.service.ReconciliationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    public ResponseEntity<ReconciliationJobResponse> runReconciliation(@Valid @RequestBody ReconciliationRunRequest request) {
        return ResponseEntity.ok(reconciliationService.runReconciliation(request.clientId()));
    }

    @GetMapping("/workspace/{clientId}")
    public ResponseEntity<ReconciliationWorkspaceResponse> getWorkspace(@PathVariable UUID clientId) {
        return ResponseEntity.ok(reconciliationService.getWorkspace(clientId));
    }

    @PostMapping("/matches/{matchResultId}/review")
    public ResponseEntity<MatchResultResponse> reviewMatch(
            @PathVariable UUID matchResultId,
            @Valid @RequestBody MatchReviewRequest request) {
        return ResponseEntity.ok(reconciliationService.reviewMatch(matchResultId, request.action()));
    }
}
