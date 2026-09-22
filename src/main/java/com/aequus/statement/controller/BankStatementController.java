package com.aequus.statement.controller;

import com.aequus.statement.dto.StatementDtos.*;
import com.aequus.statement.service.BankStatementImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/statements")
public class BankStatementController {

    private final BankStatementImportService bankStatementImportService;

    public BankStatementController(BankStatementImportService bankStatementImportService) {
        this.bankStatementImportService = bankStatementImportService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BankStatementParseResponse> parseStatement(
            @RequestParam("clientId") UUID clientId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bankStatementImportService.parseStatement(clientId, file));
    }

    @PostMapping("/confirm")
    public ResponseEntity<BankStatementImportResultResponse> confirmImport(
            @Valid @RequestBody ConfirmBankStatementImportRequest request) {
        return ResponseEntity.ok(bankStatementImportService.confirmImport(request));
    }
}
