package com.bloodbond.assistant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroqChatClientTest {
    @Test
    void normalizesQuotedApiKeyWithoutLoggingOrExposingIt() {
        assertEquals("test-key", GroqChatClient.normalizeApiKey("\"test-key\""));
        assertEquals("test-key", GroqChatClient.normalizeApiKey("'test-key'"));
    }

    @Test
    void treatsNullKeyAsMissing() {
        assertTrue(GroqChatClient.normalizeApiKey(null).isBlank());
    }
}
