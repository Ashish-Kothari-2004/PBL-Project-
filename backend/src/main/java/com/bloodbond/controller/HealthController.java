package com.bloodbond.controller;

import com.bloodbond.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.<Map<String, String>>builder()
            .status(HttpStatus.OK.value())
            .message("BloodBond service is running")
            .data(Map.of("status", "UP"))
            .build();
    }
}
