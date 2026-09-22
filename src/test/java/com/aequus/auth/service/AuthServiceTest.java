package com.aequus.auth.service;

import com.aequus.auth.dto.AuthResponse;
import com.aequus.auth.dto.LoginRequest;
import com.aequus.auth.dto.RegisterRequest;
import com.aequus.common.exception.ConflictException;
import com.aequus.common.exception.UnauthorizedException;
import com.aequus.common.security.JwtService;
import com.aequus.organization.entity.Organization;
import com.aequus.organization.service.OrganizationService;
import com.aequus.user.entity.User;
import com.aequus.user.entity.UserRole;
import com.aequus.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Organization testOrg;
    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testOrg = new Organization("Alex Firm");
        testUser = new User("Alex Mercer", "alex@example.com", "hashed_password", testOrg, UserRole.ADMIN);
    }

    @Test
    void register_WhenValidRequest_ShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("Alex Mercer", "alex@example.com", "SecurePass123!", "Alex Firm");

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(organizationService.createDefaultOrganization("Alex Firm")).thenReturn(testOrg);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(), eq("alex@example.com"))).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("mocked_jwt_token");
        assertThat(response.user().email()).isEqualTo("alex@example.com");
        assertThat(response.user().name()).isEqualTo("Alex Mercer");
        verify(organizationService).createDefaultOrganization("Alex Firm");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenNoOrganizationProvided_ShouldDeriveDefaultOrgName() {
        RegisterRequest request = new RegisterRequest("Alex Mercer", "alex@example.com", "SecurePass123!", null);

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(organizationService.createDefaultOrganization("Alex Mercer's Firm")).thenReturn(testOrg);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(), eq("alex@example.com"))).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("mocked_jwt_token");
        verify(organizationService).createDefaultOrganization("Alex Mercer's Firm");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowConflictException() {
        RegisterRequest request = new RegisterRequest("Alex Mercer", "alex@example.com", "SecurePass123!", "Alex Firm");

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(organizationService, never()).createDefaultOrganization(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest("alex@example.com", "SecurePass123!");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecurePass123!", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("alex@example.com"))).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("mocked_jwt_token");
        assertThat(response.user().email()).isEqualTo("alex@example.com");
    }

    @Test
    void login_WhenInvalidPassword_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("alex@example.com", "WrongPassword");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_WhenEmailNotFound_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "SecurePass123!");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }
}

