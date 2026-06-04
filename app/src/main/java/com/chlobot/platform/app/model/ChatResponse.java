package com.chlobot.platform.app.model;

public record ChatResponse(String content, String provider, String model, boolean mock, ChatIntentResponse intent) {

    public ChatResponse(String content, String provider, String model, boolean mock) {
        this(content, provider, model, mock, null);
    }
}
