package com.bloodbond.auth;

import com.bloodbond.auth.controller.AuthController;
import com.bloodbond.auth.dto.AuthResponse;
import com.bloodbond.auth.service.AuthService;
import com.bloodbond.config.SecurityConfig;
import com.bloodbond.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import com.bloodbond.user.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_shouldReturnCreatedForValidRequest() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse(
            "jwt-token",
            new AuthResponse.UserSummary(1L, "Alice", "alice@example.com", "DONOR")
        ));

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {"name":"Alice","email":"alice@example.com","password":"Secret123!","role":"DONOR"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.user.role").value("DONOR"));
    }

    @Test
    void register_shouldRejectWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {"name":"Alice","email":"alice@example.com","password":"password","role":"DONOR"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_shouldRejectMalformedEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {"email":"invalid","password":"Secret123!"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {"email":"alice@example.com","password":"WrongPassword123!"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
