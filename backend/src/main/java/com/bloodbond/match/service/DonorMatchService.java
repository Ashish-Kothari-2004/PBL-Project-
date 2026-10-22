package com.bloodbond.match.service;

import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.match.dto.DonorMatchResponse;
import com.bloodbond.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class DonorMatchService {
    private static final Set<String> BLOOD_GROUPS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private final DonorProfileRepository donorProfileRepository;

    public DonorMatchService(DonorProfileRepository donorProfileRepository) {
        this.donorProfileRepository = donorProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<DonorMatchResponse> search(String bloodGroup, double latitude, double longitude, int radiusKm) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
            || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new BadRequestException("Valid latitude and longitude are required");
        }
        if (radiusKm < 1 || radiusKm > 250) {
            throw new BadRequestException("Radius must be between 1 and 250 km");
        }
        if (bloodGroup != null && !BLOOD_GROUPS.contains(bloodGroup)) {
            throw new BadRequestException("Unsupported blood group");
        }

        return donorProfileRepository.findByIsAvailableTrueAndApproxLatIsNotNullAndApproxLngIsNotNull().stream()
            .filter(profile -> bloodGroup == null || bloodGroup.equals(profile.getBloodGroup()))
            .filter(profile -> profile.getNextEligibleDate() == null || !profile.getNextEligibleDate().isAfter(LocalDate.now()))
            .map(profile -> {
                double approxLat = roundCoordinate(profile.getApproxLat());
                double approxLng = roundCoordinate(profile.getApproxLng());
                return new NearbyDonor(profile, approxLat, approxLng,
                    distanceKm(latitude, longitude, approxLat, approxLng));
            })
            .filter(match -> match.distanceKm() <= radiusKm)
            .sorted(Comparator.comparingDouble(NearbyDonor::distanceKm))
            .map(this::toMatch)
            .toList();
    }

    private DonorMatchResponse toMatch(NearbyDonor donor) {
        DonorProfile profile = donor.profile();
        return new DonorMatchResponse(
            profile.getId(),
            profile.getUser().getName(),
            profile.getBloodGroup(),
            profile.getCity(),
            donor.latitude(),
            donor.longitude(),
            Math.round(donor.distanceKm() * 10.0) / 10.0
        );
    }

    private double roundCoordinate(double coordinate) {
        return Math.round(coordinate * 100.0) / 100.0;
    }

    private double distanceKm(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double deltaLat = lat2 - lat1;
        double deltaLng = Math.toRadians(longitude2 - longitude1);
        double haversine = Math.pow(Math.sin(deltaLat / 2), 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLng / 2), 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private record NearbyDonor(DonorProfile profile, double latitude, double longitude, double distanceKm) {}
}
