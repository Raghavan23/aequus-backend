package com.aequus.transaction.controller;

import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionRequest;
import com.aequus.transaction.dto.BankTransactionDtos.BankTransactionResponse;
import com.aequus.transaction.dto.BankTransactionDtos.TransactionSummaryResponse;
import com.aequus.transaction.entity.AccountingHead;
import com.aequus.transaction.entity.MatchStatus;
import com.aequus.transaction.service.BankTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class BankTransactionController {

    private final BankTransactionService bankTransactionService;

    public BankTransactionController(BankTransactionService bankTransactionService) {
        this.bankTransactionService = bankTransactionService;
    }

    @PostMapping
    public ResponseEntity<BankTransactionResponse> create(@Valid @RequestBody BankTransactionRequest request) {
        BankTransactionResponse response = bankTransactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<BankTransactionResponse>> getByClient(
            @PathVariable UUID clientId,
            @RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(bankTransactionService.getTransactionsByClient(clientId, status));
    }

    @GetMapping("/client/{clientId}/summary")
    public ResponseEntity<TransactionSummaryResponse> getSummary(@PathVariable UUID clientId) {
        return ResponseEntity.ok(bankTransactionService.getClientSummary(clientId));
    }

    @PatchMapping("/{id}/accounting-head")
    public ResponseEntity<BankTransactionResponse> updateHead(
            @PathVariable UUID id,
            @RequestParam AccountingHead head) {
        return ResponseEntity.ok(bankTransactionService.updateAccountingHead(id, head));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bankTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
