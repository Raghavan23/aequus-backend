package com.aequus.invoice.service;

import com.aequus.client.entity.Client;
import com.aequus.client.repository.ClientRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.invoice.dto.InvoiceDtos.InvoiceRequest;
import com.aequus.invoice.dto.InvoiceDtos.InvoiceResponse;
import com.aequus.invoice.entity.Invoice;
import com.aequus.invoice.entity.InvoiceSourceType;
import com.aequus.invoice.repository.InvoiceRepository;
import com.aequus.organization.entity.Organization;
import com.aequus.transaction.entity.MatchStatus;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          UserRepository userRepository,
                          CurrentUserProvider currentUserProvider) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    private User getCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Organization getOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("User does not belong to any organization");
        }
        return user.getOrganization();
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Client client = clientRepository.findByIdAndOrganizationId(request.clientId(), org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        Invoice invoice = new Invoice(
                org,
                client,
                request.invoiceNumber().trim(),
                request.vendorName().trim(),
                request.vendorGstin(),
                request.invoiceDate(),
                request.dueDate(),
                request.subtotal(),
                request.gstAmount(),
                request.totalAmount(),
                request.currency(),
                request.sourceType() != null ? request.sourceType() : InvoiceSourceType.MANUAL,
                request.rawExtractedJson(),
                request.imageUrl()
        );

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByClient(UUID clientId, MatchStatus status) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        clientRepository.findByIdAndOrganizationId(clientId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        List<Invoice> list = (status != null)
                ? invoiceRepository.findAllByClientIdAndMatchStatusOrderByInvoiceDateDesc(clientId, status)
                : invoiceRepository.findAllByClientIdOrderByInvoiceDateDesc(clientId);

        return list.stream().map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Invoice invoice = invoiceRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public void delete(UUID id) {
        User user = getCurrentUser();
        Organization org = getOrganization(user);

        Invoice invoice = invoiceRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        invoiceRepository.delete(invoice);
    }
}
