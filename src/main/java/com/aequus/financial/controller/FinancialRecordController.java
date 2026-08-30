package com.aequus.financial.controller;

import com.aequus.financial.dto.FinancialRecordRequest;
import com.aequus.financial.dto.FinancialRecordResponse;
import com.aequus.financial.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial-records")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    public FinancialRecordController(FinancialRecordService financialRecordService) {
        this.financialRecordService = financialRecordService;
    }

    @PostMapping
    public ResponseEntity<FinancialRecordResponse> create(@Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialRecordService.create(request));
    }

    @GetMapping
    public List<FinancialRecordResponse> getAll() {
        return financialRecordService.getAllForCurrentUser();
    }

    @GetMapping("/{id}")
    public FinancialRecordResponse getById(@PathVariable UUID id) {
        return financialRecordService.getById(id);
    }

    @PutMapping("/{id}")
    public FinancialRecordResponse update(@PathVariable UUID id, @Valid @RequestBody FinancialRecordRequest request) {
        return financialRecordService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        financialRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
