package com.bloodbond.request;

import com.bloodbond.chat.ChatService;
import com.bloodbond.common.exception.BadRequestException;
import com.bloodbond.common.exception.ResourceNotFoundException;
import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.request.dto.*;
import com.bloodbond.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BloodRequestService {
    public record ActionOutcome(BloodRequestView request, ChatService.AcceptedRequestRoom acceptedRoom) {}
    private final BloodRequestRepository requestRepository;
    private final BloodRequestResponseRepository responseRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final ChatService chatService;

    public BloodRequestService(BloodRequestRepository requestRepository,
                               BloodRequestResponseRepository responseRepository,
                               DonorProfileRepository donorProfileRepository,
                               ChatService chatService) {
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.chatService = chatService;
    }

    @Transactional
    public BloodRequestView create(User recipient, CreateBloodRequestRequest payload) {
        validateCoordinates(payload.approxLat(), payload.approxLng());
        BloodRequest request = new BloodRequest();
        request.setRecipient(recipient);
        request.setBloodGroup(payload.bloodGroup().trim().toUpperCase());
        request.setCity(payload.city() == null ? null : payload.city().trim());
        request.setApproxLat(payload.approxLat());
        request.setApproxLng(payload.approxLng());
        request.setRadiusKm(payload.radiusKm());
        return toView(requestRepository.save(request), true);
    }

    @Transactional(readOnly = true)
    public List<BloodRequestView> openRequests(User donor) {
        DonorProfile profile = donorProfileRepository.findByUserId(donor.getId())
            .orElseThrow(() -> new BadRequestException("Complete your donor profile to see requests"));
        return requestRepository.findOpenUnansweredByDonor(donor.getId())
            .stream()
            .filter(request -> isWithinRequestRadius(profile, request))
            .map(request -> toView(request, false))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BloodRequestView> mine(User recipient) {
        return requestRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId())
            .stream().map(request -> toView(request, true)).toList();
    }

    @Transactional
    public BloodRequestView cancel(User recipient, Long requestId) {
        BloodRequest request = requestRepository.findByIdAndRecipientIdAndStatus(requestId, recipient.getId(), "OPEN")
            .orElseThrow(() -> new ResourceNotFoundException("Open blood request not found"));
        request.setStatus("CANCELLED");
        return toView(request, true);
    }

    @Transactional
    public ActionOutcome respond(User donor, Long requestId, String decision) {
        DonorProfile profile = donorProfileRepository.findByUserId(donor.getId())
            .orElseThrow(() -> new BadRequestException("Complete your donor profile before responding"));
        if (!profile.isAvailable()) {
            throw new BadRequestException("Mark your donor profile available before responding");
        }

        BloodRequest request = requestRepository.findOpenForUpdate(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Open blood request not found"));
        if (request.getRecipient().getId().equals(donor.getId())) {
            throw new BadRequestException("You cannot respond to your own request");
        }
        if (!isWithinRequestRadius(profile, request)) {
            throw new ResourceNotFoundException("Open blood request not found");
        }
        if (responseRepository.findByBloodRequestIdAndDonorId(requestId, donor.getId()).isPresent()) {
            throw new BadRequestException("You have already responded to this request");
        }

        String status = decision.trim().toUpperCase();
        BloodRequestResponseEntity response = new BloodRequestResponseEntity();
        response.setBloodRequest(request);
        response.setDonor(donor);
        response.setStatus(status);
        responseRepository.save(response);

        ChatService.AcceptedRequestRoom acceptedRoom = null;
        if (status.equals("ACCEPTED")) {
            request.setStatus("FULFILLED");
            acceptedRoom = chatService.createRoomForRequest(donor, request);
        }
        return new ActionOutcome(toView(request, false), acceptedRoom);
    }

    private BloodRequestView toView(BloodRequest request, boolean includeResponses) {
        List<BloodRequestResponseView> responses = includeResponses
            ? responseRepository.findByBloodRequestIdOrderByCreatedAtAsc(request.getId()).stream()
                .map(response -> new BloodRequestResponseView(response.getStatus(), response.getCreatedAt()))
                .toList()
            : List.of();
        return new BloodRequestView(
            request.getId(), request.getBloodGroup(), request.getCity(), request.getRadiusKm(),
            request.getStatus(), request.getCreatedAt(), responses
        );
    }

    private void validateCoordinates(Double lat, Double lng) {
        if ((lat == null) != (lng == null)) {
            throw new BadRequestException("Provide both approximate coordinates or neither");
        }
        if (lat != null && (lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new BadRequestException("Approximate coordinates are out of range");
        }
    }

    private boolean isWithinRequestRadius(DonorProfile donor, BloodRequest request) {
        if (donor.getApproxLat() != null && donor.getApproxLng() != null
            && request.getApproxLat() != null && request.getApproxLng() != null) {
            double latitudeDifference = Math.toRadians(request.getApproxLat() - donor.getApproxLat());
            double longitudeDifference = Math.toRadians(request.getApproxLng() - donor.getApproxLng());
            double startLatitude = Math.toRadians(donor.getApproxLat());
            double endLatitude = Math.toRadians(request.getApproxLat());
            double haversine = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(startLatitude) * Math.cos(endLatitude)
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
            double normalizedHaversine = Math.min(1, Math.max(0, haversine));
            double distanceKm = 6371 * 2 * Math.atan2(
                Math.sqrt(normalizedHaversine), Math.sqrt(1 - normalizedHaversine)
            );
            return distanceKm <= request.getRadiusKm();
        }
        return donor.getCity() != null && request.getCity() != null
            && donor.getCity().trim().equalsIgnoreCase(request.getCity().trim());
    }
}
