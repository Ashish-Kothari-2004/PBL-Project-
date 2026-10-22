package com.bloodbond.request.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBloodRequestRequest(
    @NotBlank(message = "Blood group is required")
    @Pattern(regexp = "A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-", message = "Choose a supported blood group")
    String bloodGroup,
    @Size(max = 120, message = "City must be 120 characters or fewer")
    String city,
    Double approxLat,
    Double approxLng,
    @Min(value = 1, message = "Radius must be at least 1 km")
    @Max(value = 250, message = "Radius cannot exceed 250 km")
    int radiusKm
) {}
