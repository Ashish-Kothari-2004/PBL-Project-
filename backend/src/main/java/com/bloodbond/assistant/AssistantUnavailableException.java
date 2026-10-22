package com.bloodbond.assistant;

public class AssistantUnavailableException extends RuntimeException {
    public AssistantUnavailableException() {
        super("The BloodBond assistant is temporarily unavailable. Please try again shortly.");
    }

    public AssistantUnavailableException(String message) {
        super(message);
    }
}
