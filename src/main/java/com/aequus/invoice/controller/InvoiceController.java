package com.aequus.invoice.controller;

import com.aequus.invoice.dto.InvoiceDtos.InvoiceRequest;
import com.aequus.invoice.dto.InvoiceDtos.InvoiceResponse;
import com.aequus.invoice.dto.InvoiceDtos.ParsedInvoiceResponse;
import com.aequus.invoice.entity.InvoiceSourceType;
import com.aequus.invoice.service.InvoiceService;
import com.aequus.invoice.service.InvoiceVisionService;
import com.aequus.transaction.entity.MatchStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceVisionService invoiceVisionService;

    public InvoiceController(InvoiceService invoiceService, InvoiceVisionService invoiceVisionService) {
        this.invoiceService = invoiceService;
        this.invoiceVisionService = invoiceVisionService;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsedInvoiceResponse> scanInvoice(
            @RequestParam("clientId") UUID clientId,
            @RequestParam("file") MultipartFile file) {
        ParsedInvoiceResponse response = invoiceVisionService.scanAndSaveInvoice(clientId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceResponse>> getByClient(
            @PathVariable UUID clientId,
            @RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(invoiceService.getInvoicesByClient(clientId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
