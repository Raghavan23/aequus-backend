package com.aequus.financial.statement.controller;

import com.aequus.financial.statement.dto.StatementDtos.*;
import com.aequus.financial.statement.service.StatementImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping({"/api/financial-statements", "/api/v1/statements"})
public class StatementController {

    private final StatementImportService statementImportService;

    public StatementController(StatementImportService statementImportService) {
        this.statementImportService = statementImportService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StatementParseResponse> parseStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accountId") UUID accountId) {
        StatementParseResponse response = statementImportService.parseStatement(file, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<StatementImportResultResponse> confirmImport(
            @Valid @RequestBody ConfirmStatementImportRequest request) {
        StatementImportResultResponse response = statementImportService.confirmImport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
