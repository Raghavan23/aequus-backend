package com.aequus.client.service;

import com.aequus.client.dto.ClientDtos.ClientRequest;
import com.aequus.client.dto.ClientDtos.ClientResponse;
import com.aequus.client.entity.Client;
import com.aequus.client.repository.ClientRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.organization.entity.Organization;
import com.aequus.organization.repository.OrganizationRepository;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserProvider currentUserProvider;

    public ClientService(ClientRepository clientRepository,
                         UserRepository userRepository,
                         OrganizationRepository organizationRepository,
                         CurrentUserProvider currentUserProvider) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    private User getCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private Organization getEffectiveOrganization(User user) {
        if (user.getOrganization() != null) {
            return user.getOrganization();
        }
        // Fallback: create default organization if user had none
        Organization org = new Organization(user.getName() + "'s Firm");
        Organization saved = organizationRepository.save(org);
        user.setOrganization(saved);
        userRepository.save(user);
        return saved;
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);

        Client client = new Client(
                org,
                request.name().trim(),
                request.gstin(),
                request.pan(),
                request.tallyCompanyName(),
                request.contactPerson(),
                request.contactEmail(),
                request.contactPhone()
        );
        Client saved = clientRepository.save(client);
        return ClientResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        return clientRepository.findAllByOrganizationIdOrderByCreatedAtDesc(org.getId())
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getActiveClients() {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        return clientRepository.findAllByOrganizationIdAndActiveTrueOrderByNameAsc(org.getId())
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(UUID id) {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        Client client = clientRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        Client client = clientRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        client.setName(request.name().trim());
        client.setGstin(request.gstin());
        client.setPan(request.pan());
        client.setTallyCompanyName(request.tallyCompanyName());
        client.setContactPerson(request.contactPerson());
        client.setContactEmail(request.contactEmail());
        client.setContactPhone(request.contactPhone());

        Client saved = clientRepository.save(client);
        return ClientResponse.from(saved);
    }

    @Transactional
    public void toggleActive(UUID id) {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        Client client = clientRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        client.setActive(!client.isActive());
        clientRepository.save(client);
    }

    @Transactional
    public void delete(UUID id) {
        User user = getCurrentUser();
        Organization org = getEffectiveOrganization(user);
        Client client = clientRepository.findByIdAndOrganizationId(id, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        clientRepository.delete(client);
    }
}
