package com.bloodbond.request.dto;

import com.bloodbond.chat.dto.ChatRoomResponse;

public record BloodRequestActionResult(BloodRequestView request, ChatRoomResponse chatRoom) {}
