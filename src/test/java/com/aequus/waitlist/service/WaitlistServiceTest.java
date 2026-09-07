package com.aequus.waitlist.service;

import com.aequus.waitlist.dto.WaitlistRequest;
import com.aequus.waitlist.dto.WaitlistResponse;
import com.aequus.waitlist.entity.Waitlist;
import com.aequus.waitlist.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private WaitlistService waitlistService;

    @Test
    void joinWaitlist_WhenNewEmail_ShouldSaveAndReturnConfirmation() {
        WaitlistRequest request = new WaitlistRequest("alex@example.com");
        Waitlist saved = new Waitlist("alex@example.com");

        when(waitlistRepository.findByEmail("alex@example.com")).thenReturn(Optional.empty());
        when(waitlistRepository.save(any(Waitlist.class))).thenReturn(saved);

        WaitlistResponse response = waitlistService.joinWaitlist(request);

        assertThat(response.getEmail()).isEqualTo("alex@example.com");
        assertThat(response.getMessage()).contains("Priority clearance confirmed");
        verify(waitlistRepository).save(any(Waitlist.class));
    }

    @Test
    void joinWaitlist_WhenExistingEmail_ShouldReturnExistingWithoutDuplicateSave() {
        WaitlistRequest request = new WaitlistRequest("alex@example.com");
        Waitlist existing = new Waitlist("alex@example.com");

        when(waitlistRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(existing));

        WaitlistResponse response = waitlistService.joinWaitlist(request);

        assertThat(response.getEmail()).isEqualTo("alex@example.com");
        assertThat(response.getMessage()).contains("already registered");
        verify(waitlistRepository, never()).save(any(Waitlist.class));
    }
}
