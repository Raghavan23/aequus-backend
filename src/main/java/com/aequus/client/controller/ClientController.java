package com.aequus.client.controller;

import com.aequus.client.dto.ClientDtos.ClientRequest;
import com.aequus.client.dto.ClientDtos.ClientResponse;
import com.aequus.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<ClientResponse> response = activeOnly ? clientService.getActiveClients() : clientService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) {
        clientService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
