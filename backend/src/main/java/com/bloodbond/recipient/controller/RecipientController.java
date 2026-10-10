package com.bloodbond.recipient.controller;

import com.bloodbond.common.ApiResponse;
import com.bloodbond.recipient.dto.RecipientProfileRequest;
import com.bloodbond.recipient.dto.RecipientProfileResponse;
import com.bloodbond.recipient.service.RecipientProfileService;
import com.bloodbond.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('RECIPIENT')")
public class RecipientController {
    private final RecipientProfileService recipientProfileService;

    public RecipientController(RecipientProfileService recipientProfileService) {
        this.recipientProfileService = recipientProfileService;
    }

    @GetMapping("/recipients/me")
    public ResponseEntity<ApiResponse<RecipientProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        RecipientProfileResponse response = recipientProfileService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.<RecipientProfileResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Recipient profile loaded")
            .data(response)
            .build());
    }

    @PutMapping("/recipients/me")
    public ResponseEntity<ApiResponse<RecipientProfileResponse>> updateMyProfile(@AuthenticationPrincipal User user,
                                                                               @Valid @RequestBody RecipientProfileRequest request) {
        RecipientProfileResponse response = recipientProfileService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.<RecipientProfileResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Recipient profile updated")
            .data(response)
            .build());
    }
}
