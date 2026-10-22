package com.bloodbond.request.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BloodRequestView(
    Long id,
    String bloodGroup,
    String city,
    int radiusKm,
    String status,
    LocalDateTime createdAt,
    List<BloodRequestResponseView> responses
) {}
