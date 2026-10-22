package com.bloodbond.assistant;

import com.bloodbond.assistant.dto.AssistantMessageRequest;
import com.bloodbond.assistant.dto.AssistantMessageResponse;
import com.bloodbond.assistant.dto.AssistantExchangeResponse;
import com.bloodbond.common.ApiResponse;
import com.bloodbond.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
@PreAuthorize("hasAnyRole('DONOR', 'RECIPIENT')")
public class AssistantController {
    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<AssistantMessageResponse>>> history(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<AssistantMessageResponse>>builder()
            .status(HttpStatus.OK.value()).message("Assistant history loaded")
            .data(assistantService.history(user)).build());
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<AssistantExchangeResponse>> send(
        @AuthenticationPrincipal User user, @Valid @RequestBody AssistantMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<AssistantExchangeResponse>builder()
            .status(HttpStatus.OK.value()).message("Assistant replied")
            .data(assistantService.send(user, request.content())).build());
    }

    @DeleteMapping("/messages")
    public ResponseEntity<ApiResponse<Void>> clear(@AuthenticationPrincipal User user) {
        assistantService.clear(user);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value()).message("Assistant history deleted").build());
    }
}
