package com.bloodbond.auth;

import com.bloodbond.auth.dto.AuthResponse;
import com.bloodbond.auth.dto.LoginRequest;
import com.bloodbond.auth.dto.RegisterRequest;
import com.bloodbond.auth.service.AuthService;
import com.bloodbond.security.JwtService;
import com.bloodbond.user.Role;
import com.bloodbond.user.User;
import com.bloodbond.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("Alice", " Alice@Example.com ", "Secret123!", Role.DONOR);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("token-123");

        AuthResponse response = authService.register(request);

        assertEquals("token-123", response.token());
        assertEquals("alice@example.com", response.user().email());
        assertEquals("DONOR", response.user().role());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
            user.getEmail().equals("alice@example.com") &&
                passwordEncoder.matches("Secret123!", user.getPassword())
        ));
    }

    @Test
    void login_shouldNormalizeEmailAndReturnToken() {
        User user = new User("Alice", "alice@example.com", passwordEncoder.encode("Secret123!"), Role.DONOR);
        user.setId(1L);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(java.util.Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token-123");

        AuthResponse response = authService.login(new LoginRequest(" Alice@Example.com ", "Secret123!"));

        assertEquals("token-123", response.token());
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(eq("alice@example.com"));
    }
}
