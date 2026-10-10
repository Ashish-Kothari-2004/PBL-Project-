package com.bloodbond.donor.service;

import com.bloodbond.common.exception.ResourceNotFoundException;
import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.donor.dto.DonorProfileRequest;
import com.bloodbond.donor.dto.DonorProfileResponse;
import com.bloodbond.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonorProfileService {
    private final DonorProfileRepository donorProfileRepository;

    public DonorProfileService(DonorProfileRepository donorProfileRepository) {
        this.donorProfileRepository = donorProfileRepository;
    }

    @Transactional(readOnly = true)
    public DonorProfileResponse getProfile(User user) {
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                DonorProfile empty = new DonorProfile();
                empty.setUser(user);
                empty.setBloodGroup("O+");
                empty.setDateOfBirth(null);
                empty.setAvailable(true);
                empty.setVerificationStatus("PENDING");
                return empty;
            });

        return toResponse(profile);
    }

    @Transactional
    public DonorProfileResponse updateProfile(User user, DonorProfileRequest request) {
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
            .orElse(new DonorProfile());

        profile.setUser(user);
        profile.setBloodGroup(request.bloodGroup());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setCity(request.city());
        profile.setApproxLat(request.approxLat());
        profile.setApproxLng(request.approxLng());
        profile.setAvailable(request.isAvailable() != null ? request.isAvailable() : true);
        if (profile.getVerificationStatus() == null || profile.getVerificationStatus().isBlank()) {
            profile.setVerificationStatus("PENDING");
        }

        DonorProfile saved = donorProfileRepository.save(profile);
        return toResponse(saved);
    }

    private DonorProfileResponse toResponse(DonorProfile profile) {
        return new DonorProfileResponse(
            profile.getId(),
            profile.getUser() != null ? profile.getUser().getId() : null,
            profile.getBloodGroup(),
            profile.getDateOfBirth(),
            profile.getCity(),
            profile.getApproxLat(),
            profile.getApproxLng(),
            profile.isAvailable(),
            profile.getVerificationStatus()
        );
    }
}
