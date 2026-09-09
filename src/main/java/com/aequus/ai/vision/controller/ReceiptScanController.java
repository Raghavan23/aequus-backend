package com.aequus.ai.vision.controller;

import com.aequus.ai.vision.dto.ReceiptScanDtos.ConfirmReceiptRequest;
import com.aequus.ai.vision.dto.ReceiptScanDtos.ParsedReceiptResponse;
import com.aequus.ai.vision.dto.ReceiptScanDtos.ReceiptScanSummaryResponse;
import com.aequus.ai.vision.service.ReceiptVisionService;
import com.aequus.financial.dto.FinancialRecordResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai/receipts")
public class ReceiptScanController {

    private final ReceiptVisionService receiptVisionService;

    public ReceiptScanController(ReceiptVisionService receiptVisionService) {
        this.receiptVisionService = receiptVisionService;
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsedReceiptResponse> scanReceipt(@RequestParam("file") MultipartFile file) {
        ParsedReceiptResponse response = receiptVisionService.scanReceipt(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<FinancialRecordResponse> confirmAndLog(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmReceiptRequest request) {
        FinancialRecordResponse response = receiptVisionService.confirmAndLog(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ReceiptScanSummaryResponse> getRecentScans() {
        return receiptVisionService.getRecentScans();
    }
}
