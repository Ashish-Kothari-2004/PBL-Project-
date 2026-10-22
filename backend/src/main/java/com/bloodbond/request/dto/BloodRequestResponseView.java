package com.bloodbond.request.dto;

import java.time.LocalDateTime;

public record BloodRequestResponseView(String status, LocalDateTime createdAt) {}
