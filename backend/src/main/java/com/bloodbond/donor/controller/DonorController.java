package com.bloodbond.donor.controller;

import com.bloodbond.common.ApiResponse;
import com.bloodbond.donor.dto.DonorProfileRequest;
import com.bloodbond.donor.dto.DonorProfileResponse;
import com.bloodbond.donor.service.DonorProfileService;
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
@PreAuthorize("hasRole('DONOR')")
public class DonorController {
    private final DonorProfileService donorProfileService;

    public DonorController(DonorProfileService donorProfileService) {
        this.donorProfileService = donorProfileService;
    }

    @GetMapping("/donors/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        DonorProfileResponse response = donorProfileService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.<DonorProfileResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Donor profile loaded")
            .data(response)
            .build());
    }

    @PutMapping("/donors/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateMyProfile(@AuthenticationPrincipal User user,
                                                                             @Valid @RequestBody DonorProfileRequest request) {
        DonorProfileResponse response = donorProfileService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.<DonorProfileResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Donor profile updated")
            .data(response)
            .build());
    }
}
