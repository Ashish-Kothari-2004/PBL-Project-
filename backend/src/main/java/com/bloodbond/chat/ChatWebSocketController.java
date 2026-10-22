package com.bloodbond.chat;

import com.bloodbond.chat.dto.ChatMessageResponse;
import com.bloodbond.chat.dto.SendChatMessageRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void sendMessage(
        @DestinationVariable Long roomId,
        @Valid @Payload SendChatMessageRequest request,
        Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof com.bloodbond.user.User user)) {
            throw new AccessDeniedException("Chat connection is not authenticated");
        }
        ChatMessageResponse message = chatService.sendMessage(user, roomId, request);
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, message);
    }
}
