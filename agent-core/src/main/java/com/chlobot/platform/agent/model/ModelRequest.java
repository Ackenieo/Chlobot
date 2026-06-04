package com.chlobot.platform.agent.model;

import org.springframework.ai.chat.prompt.Prompt;

import java.time.Duration;
import java.util.Map;

public record ModelRequest(Prompt prompt, Duration timeout, Map<String, Object> metadata) {

    public ModelRequest {
        if (prompt == null) {
            throw new IllegalArgumentException("prompt must not be null");
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }

    public static ModelRequest of(String userInput) {
        return new ModelRequest(new Prompt(userInput), null, Map.of());
    }
}
