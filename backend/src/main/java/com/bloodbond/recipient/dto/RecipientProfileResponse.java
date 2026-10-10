package com.bloodbond.recipient.dto;

public record RecipientProfileResponse(
    Long id,
    Long userId,
    String city,
    Double approxLat,
    Double approxLng
) {}
