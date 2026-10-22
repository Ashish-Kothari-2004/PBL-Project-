package com.bloodbond.chat.security;

import com.bloodbond.chat.ChatService;
import com.bloodbond.common.exception.ResourceNotFoundException;
import com.bloodbond.security.JwtService;
import com.bloodbond.user.User;
import com.bloodbond.user.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatWebSocketAuthInterceptor implements ChannelInterceptor {
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/chat/rooms/(\\d+)$");
    private static final Pattern ROOM_SEND = Pattern.compile("^/app/chat/rooms/(\\d+)/messages$");
    private static final String ROOM_LIST_QUEUE = "/user/queue/chat/rooms";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatService chatService;

    public ChatWebSocketAuthInterceptor(JwtService jwtService, UserRepository userRepository, ChatService chatService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
            return message;
        }

        if (accessor.getCommand() == StompCommand.DISCONNECT || accessor.getCommand() == StompCommand.UNSUBSCRIBE) {
            return message;
        }

        User user = authenticatedUser(accessor);
        String destination = accessor.getDestination();
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(user, destination);
        } else if (accessor.getCommand() == StompCommand.SEND) {
            authorizeSend(user, destination);
        } else {
            throw new AccessDeniedException("Unsupported chat socket command");
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("A bearer token is required for chat");
        }

        try {
            String username = jwtService.extractUsername(authorization.substring(7));
            User user = userRepository.findByEmail(username)
                .filter(candidate -> jwtService.isTokenValid(authorization.substring(7), candidate))
                .orElseThrow(() -> new AccessDeniedException("Invalid chat authentication"));
            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid chat authentication", exception);
        }
    }

    private User authenticatedUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication
            && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("Chat connection is not authenticated");
    }

    private void authorizeSubscription(User user, String destination) {
        if (ROOM_LIST_QUEUE.equals(destination)) {
            return;
        }
        Matcher roomTopic = ROOM_TOPIC.matcher(destination == null ? "" : destination);
        if (!roomTopic.matches()) {
            throw new AccessDeniedException("Chat subscription is not allowed");
        }
        requireParticipant(user, Long.parseLong(roomTopic.group(1)));
    }

    private void authorizeSend(User user, String destination) {
        Matcher roomSend = ROOM_SEND.matcher(destination == null ? "" : destination);
        if (!roomSend.matches()) {
            throw new AccessDeniedException("Chat destination is not allowed");
        }
        requireParticipant(user, Long.parseLong(roomSend.group(1)));
    }

    private void requireParticipant(User user, Long roomId) {
        try {
            chatService.verifyParticipant(user, roomId);
        } catch (ResourceNotFoundException exception) {
            throw new AccessDeniedException("Chat room is not available to this user", exception);
        }
    }
}
