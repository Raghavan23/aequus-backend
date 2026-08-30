package com.aequus.financial.service;

import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.dto.FinancialRecordRequest;
import com.aequus.financial.dto.FinancialRecordResponse;
import com.aequus.financial.entity.FinancialRecord;
import com.aequus.financial.repository.FinancialRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final CurrentUserProvider currentUserProvider;

    public FinancialRecordService(FinancialRecordRepository financialRecordRepository,
                                   CurrentUserProvider currentUserProvider) {
        this.financialRecordRepository = financialRecordRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public FinancialRecordResponse create(FinancialRecordRequest request) {
        request.category().validateBelongsTo(request.type());

        UUID userId = currentUserProvider.getCurrentUserId();
        FinancialRecord record = new FinancialRecord(userId, request.type(), request.category(), request.amount());
        FinancialRecord saved = financialRecordRepository.save(record);

        return FinancialRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> getAllForCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return financialRecordRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FinancialRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getById(UUID id) {
        return FinancialRecordResponse.from(getOwnedRecordOrThrow(id));
    }

    @Transactional
    public FinancialRecordResponse update(UUID id, FinancialRecordRequest request) {
        request.category().validateBelongsTo(request.type());

        FinancialRecord record = getOwnedRecordOrThrow(id);
        record.update(request.type(), request.category(), request.amount());

        return FinancialRecordResponse.from(record);
    }

    @Transactional
    public void delete(UUID id) {
        // Ensures the record exists and belongs to the current user before deleting.
        getOwnedRecordOrThrow(id);
        financialRecordRepository.deleteByIdAndUserId(id, currentUserProvider.getCurrentUserId());
    }

    private FinancialRecord getOwnedRecordOrThrow(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return financialRecordRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found"));
    }
}
