package com.bloodbond.user;

import com.bloodbond.common.ApiResponse;
import com.bloodbond.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        User persisted = userRepository.findById(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
            .status(HttpStatus.OK.value())
            .message("User profile loaded")
            .data(new UserResponse(persisted.getId(), persisted.getName(), persisted.getEmail(), persisted.getRole().name()))
            .build());
    }

    public record UserResponse(Long id, String name, String email, String role) {}
}
