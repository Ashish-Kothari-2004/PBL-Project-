package com.bloodbond.match.controller;

import com.bloodbond.common.ApiResponse;
import com.bloodbond.match.dto.DonorMatchResponse;
import com.bloodbond.match.service.DonorMatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@PreAuthorize("hasRole('RECIPIENT')")
public class MatchController {
    private final DonorMatchService donorMatchService;

    public MatchController(DonorMatchService donorMatchService) {
        this.donorMatchService = donorMatchService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DonorMatchResponse>>> search(
        @RequestParam(required = false) String bloodGroup,
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(defaultValue = "50") int radiusKm
    ) {
        List<DonorMatchResponse> donors = donorMatchService.search(bloodGroup, lat, lng, radiusKm);
        return ResponseEntity.ok(ApiResponse.<List<DonorMatchResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Nearby donors loaded")
            .data(donors)
            .build());
    }
}
