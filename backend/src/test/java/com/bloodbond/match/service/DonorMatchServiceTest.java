package com.bloodbond.match.service;

import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.common.exception.BadRequestException;
import com.bloodbond.user.Role;
import com.bloodbond.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DonorMatchServiceTest {
    private final DonorProfileRepository donorProfileRepository = mock(DonorProfileRepository.class);
    private final DonorMatchService donorMatchService = new DonorMatchService(donorProfileRepository);

    @Test
    void searchReturnsOnlyMatchingDonorsWithinRadiusAndOmitsContactDetails() {
        DonorProfile nearby = donorProfile("A+", 0.1234, 0.004, "Alice");
        DonorProfile outsideRadius = donorProfile("A+", 0, 1, "Bob");
        DonorProfile otherBloodGroup = donorProfile("O+", 0, 0, "Charlie");
        when(donorProfileRepository.findByIsAvailableTrueAndApproxLatIsNotNullAndApproxLngIsNotNull())
            .thenReturn(List.of(nearby, outsideRadius, otherBloodGroup));

        var matches = donorMatchService.search("A+", 0.12, 0, 50);

        assertEquals(1, matches.size());
        assertEquals("Alice", matches.get(0).donorName());
        assertEquals(0, matches.get(0).distanceKm(), 0.0);
        assertEquals(0.12, matches.get(0).approxLat(), 0.0);
    }

    @Test
    void searchRejectsInvalidRadius() {
        assertThrows(BadRequestException.class, () -> donorMatchService.search(null, 0, 0, 0));
    }

    private DonorProfile donorProfile(String bloodGroup, double latitude, double longitude, String name) {
        DonorProfile profile = new DonorProfile();
        profile.setId((long) (name.charAt(0)));
        profile.setBloodGroup(bloodGroup);
        profile.setApproxLat(latitude);
        profile.setApproxLng(longitude);
        profile.setUser(new User(name, name.toLowerCase() + "@example.com", "hash", Role.DONOR));
        return profile;
    }
}
