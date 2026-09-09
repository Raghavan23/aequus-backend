package com.aequus.ai.vision.service;

import com.aequus.account.entity.Account;
import com.aequus.account.entity.AccountType;
import com.aequus.account.repository.AccountRepository;
import com.aequus.ai.vision.dto.ReceiptScanDtos.ConfirmReceiptRequest;
import com.aequus.ai.vision.dto.ReceiptScanDtos.ParsedReceiptResponse;
import com.aequus.ai.vision.dto.ReceiptScanDtos.ReceiptScanSummaryResponse;
import com.aequus.ai.vision.entity.ReceiptScan;
import com.aequus.ai.vision.repository.ReceiptScanRepository;
import com.aequus.common.exception.BadRequestException;
import com.aequus.common.exception.ResourceNotFoundException;
import com.aequus.common.security.CurrentUserProvider;
import com.aequus.financial.dto.FinancialRecordRequest;
import com.aequus.financial.dto.FinancialRecordResponse;
import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import com.aequus.financial.service.FinancialRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptVisionServiceTest {

    @Mock
    private ReceiptScanRepository receiptScanRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FinancialRecordService financialRecordService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReceiptVisionService receiptVisionService;

    private UUID userId;
    private UUID accountId;
    private UUID scanId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        scanId = UUID.randomUUID();
    }

    @Test
    void scanReceipt_WhenValidImage_ShouldParseAndSaveScan() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "starbucks_coffee.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        Account account = new Account(userId, "Checking", AccountType.CURRENT, "INR",
                new BigDecimal("1000.00"), null, null, null, null);

        when(accountRepository.findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(account));

        ReceiptScan mockScan = new ReceiptScan(
                userId,
                account.getId(),
                "Starbucks Reserve #402",
                new BigDecimal("380.00"),
                new BigDecimal("19.00"),
                BigDecimal.ZERO,
                new BigDecimal("399.00"),
                "INR",
                "FOOD",
                "{}"
        );

        when(receiptScanRepository.save(any(ReceiptScan.class))).thenReturn(mockScan);

        ParsedReceiptResponse response = receiptVisionService.scanReceipt(file);

        assertThat(response.merchant()).isEqualTo("Starbucks Reserve #402");
        assertThat(response.suggestedCategory()).isEqualTo(FinancialCategory.FOOD);
        assertThat(response.totalAmount()).isEqualByComparingTo("399.00");
        assertThat(response.taxAmount()).isEqualByComparingTo("19.00");
        assertThat(response.lineItems()).isNotEmpty();
        verify(receiptScanRepository).save(any(ReceiptScan.class));
    }

    @Test
    void scanReceipt_WhenTravelReceipt_ShouldInferTravelCategory() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uber_airport_trip.pdf",
                "application/pdf",
                "pdf stream".getBytes()
        );

        when(accountRepository.findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(userId))
                .thenReturn(List.of());

        ReceiptScan mockScan = new ReceiptScan(
                userId, null, "Uber Premier Ride", new BigDecimal("420.00"),
                new BigDecimal("21.00"), BigDecimal.ZERO, new BigDecimal("441.00"),
                "INR", "TRAVEL", "{}"
        );
        when(receiptScanRepository.save(any(ReceiptScan.class))).thenReturn(mockScan);

        ParsedReceiptResponse response = receiptVisionService.scanReceipt(file);

        assertThat(response.merchant()).isEqualTo("Uber Premier Ride");
        assertThat(response.suggestedCategory()).isEqualTo(FinancialCategory.TRAVEL);
        assertThat(response.taxRatePercentage()).isEqualTo(5.0);
    }

    @Test
    void scanReceipt_WhenClothingReceipt_ShouldInferClothingCategory() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "zara_invoice.jpg",
                "image/jpeg",
                "image stream".getBytes()
        );

        when(accountRepository.findAllByUserIdAndArchivedFalseAndIsDeletedFalseOrderByCreatedAtAsc(userId))
                .thenReturn(List.of());

        ReceiptScan mockScan = new ReceiptScan(
                userId, null, "Zara Retail Store", new BigDecimal("2499.00"),
                new BigDecimal("299.88"), BigDecimal.ZERO, new BigDecimal("2798.88"),
                "INR", "CLOTHING", "{}"
        );
        when(receiptScanRepository.save(any(ReceiptScan.class))).thenReturn(mockScan);

        ParsedReceiptResponse response = receiptVisionService.scanReceipt(file);

        assertThat(response.merchant()).isEqualTo("Zara Retail Store");
        assertThat(response.suggestedCategory()).isEqualTo(FinancialCategory.CLOTHING);
        assertThat(response.taxRatePercentage()).isEqualTo(12.0);
    }

    @Test
    void scanReceipt_WhenFileEmpty_ShouldThrowBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> receiptVisionService.scanReceipt(emptyFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be empty");

        verifyNoInteractions(receiptScanRepository);
    }

    @Test
    void scanReceipt_WhenInvalidContentType_ShouldThrowBadRequestException() {
        MockMultipartFile textFile = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> receiptVisionService.scanReceipt(textFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only image");

        verifyNoInteractions(receiptScanRepository);
    }

    @Test
    void confirmAndLog_WhenValidRequest_ShouldCreateRecordAndConfirmScan() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        ReceiptScan scan = new ReceiptScan(
                userId,
                accountId,
                "Starbucks Reserve",
                new BigDecimal("380.00"),
                new BigDecimal("19.00"),
                BigDecimal.ZERO,
                new BigDecimal("399.00"),
                "INR",
                "FOOD",
                "{}"
        );

        when(receiptScanRepository.findByIdAndUserId(scanId, userId)).thenReturn(Optional.of(scan));

        FinancialRecordResponse mockRecord = new FinancialRecordResponse(
                UUID.randomUUID(),
                accountId,
                "Checking",
                FinancialType.EXPENSE,
                FinancialCategory.FOOD,
                new BigDecimal("399.00"),
                Instant.now(),
                Instant.now()
        );

        when(financialRecordService.create(any(FinancialRecordRequest.class))).thenReturn(mockRecord);

        ConfirmReceiptRequest request = new ConfirmReceiptRequest(
                accountId,
                FinancialCategory.FOOD,
                new BigDecimal("399.00"),
                "Starbucks Reserve",
                "Coffee with team"
        );

        FinancialRecordResponse response = receiptVisionService.confirmAndLog(scanId, request);

        assertThat(response.amount()).isEqualByComparingTo("399.00");
        assertThat(scan.getStatus()).isEqualTo("CONFIRMED");
        verify(financialRecordService).create(any(FinancialRecordRequest.class));
        verify(receiptScanRepository).save(scan);
    }

    @Test
    void confirmAndLog_WhenAlreadyConfirmed_ShouldThrowBadRequestException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        ReceiptScan scan = new ReceiptScan(
                userId,
                accountId,
                "Starbucks Reserve",
                new BigDecimal("380.00"),
                new BigDecimal("19.00"),
                BigDecimal.ZERO,
                new BigDecimal("399.00"),
                "INR",
                "FOOD",
                "{}"
        );
        scan.confirm(UUID.randomUUID(), accountId);

        when(receiptScanRepository.findByIdAndUserId(scanId, userId)).thenReturn(Optional.of(scan));

        ConfirmReceiptRequest request = new ConfirmReceiptRequest(
                accountId,
                FinancialCategory.FOOD,
                new BigDecimal("399.00"),
                "Starbucks Reserve",
                null
        );

        assertThatThrownBy(() -> receiptVisionService.confirmAndLog(scanId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been confirmed");

        verifyNoInteractions(financialRecordService);
    }

    @Test
    void confirmAndLog_WhenScanNotFound_ShouldThrowResourceNotFoundException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(receiptScanRepository.findByIdAndUserId(scanId, userId)).thenReturn(Optional.empty());

        ConfirmReceiptRequest request = new ConfirmReceiptRequest(
                accountId,
                FinancialCategory.FOOD,
                new BigDecimal("399.00"),
                "Starbucks Reserve",
                null
        );

        assertThatThrownBy(() -> receiptVisionService.confirmAndLog(scanId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Receipt scan not found");
    }

    @Test
    void getRecentScans_ShouldReturnUserScans() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);

        ReceiptScan scan = new ReceiptScan(
                userId,
                accountId,
                "Uber Ride",
                new BigDecimal("420.00"),
                new BigDecimal("21.00"),
                BigDecimal.ZERO,
                new BigDecimal("441.00"),
                "INR",
                "TRAVEL",
                "{}"
        );

        when(receiptScanRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(scan));

        List<ReceiptScanSummaryResponse> list = receiptVisionService.getRecentScans();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).merchant()).isEqualTo("Uber Ride");
    }
}
