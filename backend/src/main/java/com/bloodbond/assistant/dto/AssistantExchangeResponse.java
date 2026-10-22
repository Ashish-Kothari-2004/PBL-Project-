package com.bloodbond.assistant.dto;

public record AssistantExchangeResponse(
    AssistantMessageResponse userMessage,
    AssistantMessageResponse assistantMessage
) {}
