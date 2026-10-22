package com.bloodbond.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    Long senderId,
    String senderName,
    String content,
    LocalDateTime createdAt
) {}
