package com.bloodbond.recipient.service;

import com.bloodbond.recipient.RecipientProfile;
import com.bloodbond.recipient.RecipientProfileRepository;
import com.bloodbond.recipient.dto.RecipientProfileRequest;
import com.bloodbond.recipient.dto.RecipientProfileResponse;
import com.bloodbond.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipientProfileService {
    private final RecipientProfileRepository recipientProfileRepository;

    public RecipientProfileService(RecipientProfileRepository recipientProfileRepository) {
        this.recipientProfileRepository = recipientProfileRepository;
    }

    @Transactional(readOnly = true)
    public RecipientProfileResponse getProfile(User user) {
        RecipientProfile profile = recipientProfileRepository.findByUserId(user.getId())
            .orElse(new RecipientProfile());
        profile.setUser(user);
        return toResponse(profile);
    }

    @Transactional
    public RecipientProfileResponse updateProfile(User user, RecipientProfileRequest request) {
        RecipientProfile profile = recipientProfileRepository.findByUserId(user.getId())
            .orElse(new RecipientProfile());

        profile.setUser(user);
        profile.setCity(request.city());
        profile.setApproxLat(request.approxLat());
        profile.setApproxLng(request.approxLng());

        RecipientProfile saved = recipientProfileRepository.save(profile);
        return toResponse(saved);
    }

    private RecipientProfileResponse toResponse(RecipientProfile profile) {
        return new RecipientProfileResponse(
            profile.getId(),
            profile.getUser() != null ? profile.getUser().getId() : null,
            profile.getCity(),
            profile.getApproxLat(),
            profile.getApproxLng()
        );
    }
}
