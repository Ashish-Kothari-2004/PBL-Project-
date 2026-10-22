package com.bloodbond.request;

import com.bloodbond.chat.ChatService;
import com.bloodbond.chat.dto.ChatRoomResponse;
import com.bloodbond.common.exception.BadRequestException;
import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.user.Role;
import com.bloodbond.user.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BloodRequestServiceTest {
    private final BloodRequestRepository requestRepository = mock(BloodRequestRepository.class);
    private final BloodRequestResponseRepository responseRepository = mock(BloodRequestResponseRepository.class);
    private final DonorProfileRepository donorProfileRepository = mock(DonorProfileRepository.class);
    private final ChatService chatService = mock(ChatService.class);
    private final BloodRequestService service = new BloodRequestService(
        requestRepository, responseRepository, donorProfileRepository, chatService
    );

    @Test
    void acceptingOpenRequestMarksItFulfilledAndCreatesPrivateChat() {
        User donor = new User("Donor", "donor@example.com", "hash", Role.DONOR);
        donor.setId(10L);
        User recipient = new User("Recipient", "recipient@example.com", "hash", Role.RECIPIENT);
        recipient.setId(20L);
        DonorProfile profile = new DonorProfile();
        profile.setAvailable(true);
        profile.setCity("Springfield");
        BloodRequest request = new BloodRequest();
        request.setRecipient(recipient);
        request.setBloodGroup("O+");
        request.setCity("Springfield");
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));
        when(requestRepository.findOpenForUpdate(5L)).thenReturn(Optional.of(request));
        when(responseRepository.findByBloodRequestIdAndDonorId(5L, 10L)).thenReturn(Optional.empty());
        when(chatService.createRoomForRequest(donor, request)).thenReturn(
            new ChatService.AcceptedRequestRoom(
                new ChatRoomResponse(1L, 5L, "Recipient", "O+", null, null),
                new ChatRoomResponse(1L, 5L, "Donor", "O+", null, null),
                "recipient@example.com"
            )
        );

        BloodRequestService.ActionOutcome result = service.respond(donor, 5L, "ACCEPTED");

        assertEquals("FULFILLED", request.getStatus());
        assertEquals("FULFILLED", result.request().status());
        verify(responseRepository).save(any(BloodRequestResponseEntity.class));
        verify(chatService).createRoomForRequest(donor, request);
    }

    @Test
    void decliningRequestKeepsItOpenAndDoesNotStartChat() {
        User donor = new User("Donor", "donor@example.com", "hash", Role.DONOR);
        donor.setId(10L);
        User recipient = new User("Recipient", "recipient@example.com", "hash", Role.RECIPIENT);
        recipient.setId(20L);
        DonorProfile profile = new DonorProfile();
        profile.setAvailable(true);
        profile.setCity("Springfield");
        BloodRequest request = new BloodRequest();
        request.setRecipient(recipient);
        request.setBloodGroup("O+");
        request.setCity("Springfield");
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));
        when(requestRepository.findOpenForUpdate(5L)).thenReturn(Optional.of(request));
        when(responseRepository.findByBloodRequestIdAndDonorId(5L, 10L)).thenReturn(Optional.empty());

        BloodRequestService.ActionOutcome result = service.respond(donor, 5L, "DECLINED");

        assertEquals("OPEN", result.request().status());
        assertEquals("OPEN", request.getStatus());
        verify(chatService, never()).createRoomForRequest(any(), any());
    }

    @Test
    void unavailableDonorCannotRespond() {
        User donor = new User("Donor", "donor@example.com", "hash", Role.DONOR);
        donor.setId(10L);
        DonorProfile profile = new DonorProfile();
        profile.setAvailable(false);
        when(donorProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        assertThrows(BadRequestException.class, () -> service.respond(donor, 5L, "ACCEPTED"));
        verify(requestRepository, never()).findOpenForUpdate(any());
    }
}
