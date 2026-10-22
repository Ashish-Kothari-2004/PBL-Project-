package com.bloodbond.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomResponse(
    Long id,
    Long bloodRequestId,
    String counterpartName,
    String bloodGroup,
    String city,
    LocalDateTime createdAt
) {}
