package com.bloodbond.assistant.dto;

import java.time.LocalDateTime;

public record AssistantMessageResponse(Long id, String role, String content, LocalDateTime createdAt) {}
