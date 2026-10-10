package com.bloodbond.donor.dto;

import java.time.LocalDate;

public record DonorProfileResponse(
    Long id,
    Long userId,
    String bloodGroup,
    LocalDate dateOfBirth,
    String city,
    Double approxLat,
    Double approxLng,
    boolean isAvailable,
    String verificationStatus
) {}
