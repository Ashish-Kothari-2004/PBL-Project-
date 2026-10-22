package com.bloodbond.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantMessageRequest(
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message must be 2,000 characters or fewer")
    String content
) {}
