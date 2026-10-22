package com.bloodbond.match.dto;

public record DonorMatchResponse(
    Long donorProfileId,
    String donorName,
    String bloodGroup,
    String city,
    Double approxLat,
    Double approxLng,
    double distanceKm
) {}
