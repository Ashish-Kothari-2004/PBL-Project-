package com.bloodbond.chat;

import com.bloodbond.chat.dto.ChatMessageResponse;
import com.bloodbond.chat.dto.ChatRoomResponse;
import com.bloodbond.chat.dto.CreateChatRoomRequest;
import com.bloodbond.chat.dto.SendChatMessageRequest;
import com.bloodbond.common.ApiResponse;
import com.bloodbond.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<ChatRoomResponse>>builder()
            .status(HttpStatus.OK.value()).message("Chat rooms loaded").data(chatService.getRooms(user)).build());
    }

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
        @AuthenticationPrincipal User user, @Valid @RequestBody CreateChatRoomRequest request
    ) {
        ChatService.ChatRoomCreation creation = chatService.createRoom(user, request);
        HttpStatus status = creation.created() ? HttpStatus.CREATED : HttpStatus.OK;
        if (creation.created()) {
            messagingTemplate.convertAndSendToUser(
                creation.donorUsername(),
                "/queue/chat/rooms",
                creation.donorRoom()
            );
        }
        return ResponseEntity.status(status).body(ApiResponse.<ChatRoomResponse>builder()
            .status(status.value()).message("Chat room ready").data(creation.room()).build());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
        @AuthenticationPrincipal User user, @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(ApiResponse.<List<ChatMessageResponse>>builder()
            .status(HttpStatus.OK.value()).message("Chat messages loaded").data(chatService.getMessages(user, roomId)).build());
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
        @AuthenticationPrincipal User user, @PathVariable Long roomId, @Valid @RequestBody SendChatMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ChatMessageResponse>builder()
            .status(HttpStatus.CREATED.value()).message("Message sent").data(chatService.sendMessage(user, roomId, request)).build());
    }
}
