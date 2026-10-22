package com.bloodbond.request;

import com.bloodbond.chat.ChatService;
import com.bloodbond.chat.dto.ChatRoomResponse;
import com.bloodbond.common.ApiResponse;
import com.bloodbond.request.dto.*;
import com.bloodbond.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {
    private final BloodRequestService requestService;
    private final SimpMessagingTemplate messagingTemplate;

    public BloodRequestController(BloodRequestService requestService, SimpMessagingTemplate messagingTemplate) {
        this.requestService = requestService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<ApiResponse<BloodRequestView>> create(
        @AuthenticationPrincipal User user, @Valid @RequestBody CreateBloodRequestRequest request
    ) {
        BloodRequestView created = requestService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<BloodRequestView>builder()
            .status(HttpStatus.CREATED.value()).message("Blood request created").data(created).build());
    }

    @GetMapping("/open")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<List<BloodRequestView>>> openRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<BloodRequestView>>builder()
            .status(HttpStatus.OK.value()).message("Open requests loaded").data(requestService.openRequests(user)).build());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<ApiResponse<List<BloodRequestView>>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<BloodRequestView>>builder()
            .status(HttpStatus.OK.value()).message("Your requests loaded").data(requestService.mine(user)).build());
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<ApiResponse<BloodRequestView>> cancel(
        @AuthenticationPrincipal User user, @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(ApiResponse.<BloodRequestView>builder()
            .status(HttpStatus.OK.value()).message("Blood request cancelled")
            .data(requestService.cancel(user, requestId)).build());
    }

    @PostMapping("/{requestId}/respond")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<ApiResponse<BloodRequestActionResult>> respond(
        @AuthenticationPrincipal User user,
        @PathVariable Long requestId,
        @Valid @RequestBody RespondToBloodRequest request
    ) {
        BloodRequestService.ActionOutcome outcome = requestService.respond(user, requestId, request.decision());
        ChatService.AcceptedRequestRoom acceptedRoom = outcome.acceptedRoom();
        if (acceptedRoom != null) {
            messagingTemplate.convertAndSendToUser(
                acceptedRoom.recipientUsername(), "/queue/chat/rooms", acceptedRoom.recipientRoom()
            );
        }
        BloodRequestActionResult result = new BloodRequestActionResult(
            outcome.request(), acceptedRoom == null ? null : acceptedRoom.donorRoom()
        );
        return ResponseEntity.ok(ApiResponse.<BloodRequestActionResult>builder()
            .status(HttpStatus.OK.value()).message("Response recorded").data(result).build());
    }
}
