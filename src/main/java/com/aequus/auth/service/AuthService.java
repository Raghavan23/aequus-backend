package com.aequus.auth.service;

import com.aequus.auth.dto.AuthResponse;
import com.aequus.auth.dto.LoginRequest;
import com.aequus.auth.dto.RegisterRequest;
import com.aequus.common.exception.ConflictException;
import com.aequus.common.exception.UnauthorizedException;
import com.aequus.common.security.JwtService;
import com.aequus.user.dto.UserResponse;
import com.aequus.user.entity.User;
import com.aequus.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return AuthResponse.of(token, UserResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.of(token, UserResponse.from(user));
    }
}
