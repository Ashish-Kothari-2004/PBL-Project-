package com.bloodbond.donor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDate;

public record DonorProfileRequest(
    @NotBlank(message = "Blood group is required")
    @Pattern(regexp = "^(A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-)$", message = "Blood group must be valid")
    String bloodGroup,

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,
    String city,
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    Double approxLat,
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    Double approxLng,
    Boolean isAvailable
) {
    @AssertTrue(message = "Donors must be at least 18 years old")
    public boolean isAdult() {
        return dateOfBirth != null && !dateOfBirth.isAfter(LocalDate.now().minusYears(18));
    }
}
