package com.bloodbond.auth.service;

import com.bloodbond.auth.dto.AuthResponse;
import com.bloodbond.auth.dto.LoginRequest;
import com.bloodbond.auth.dto.RegisterRequest;
import com.bloodbond.common.exception.BadRequestException;
import com.bloodbond.security.JwtService;
import com.bloodbond.user.Role;
import com.bloodbond.user.User;
import com.bloodbond.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }

        if (request.role() == null || (request.role() != Role.DONOR && request.role() != Role.RECIPIENT)) {
            throw new BadRequestException("Role must be either DONOR or RECIPIENT");
        }

        User user = new User(
            request.name(),
            email,
            passwordEncoder.encode(request.password()),
            request.role()
        );

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, new AuthResponse.UserSummary(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        ));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, new AuthResponse.UserSummary(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        ));
    }
}
