package com.bloodbond.donor;

import com.bloodbond.donor.dto.DonorProfileRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DonorProfileRequestTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsDonorsYoungerThanEighteen() {
        DonorProfileRequest request = new DonorProfileRequest(
            "O+",
            LocalDate.now().minusYears(18).plusDays(1),
            "Springfield",
            null,
            null,
            true
        );

        assertTrue(validator.validate(request).stream()
            .anyMatch(violation -> violation.getMessage().equals("Donors must be at least 18 years old")));
    }

    @Test
    void acceptsDonorsWhoAreAtLeastEighteen() {
        DonorProfileRequest request = new DonorProfileRequest(
            "O+",
            LocalDate.now().minusYears(18),
            "Springfield",
            null,
            null,
            true
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}
