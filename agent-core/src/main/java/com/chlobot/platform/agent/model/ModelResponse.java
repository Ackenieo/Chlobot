package com.chlobot.platform.agent.model;

import java.util.Map;

public record ModelResponse(String content, String provider, String model, boolean mock, Map<String, Object> metadata) {

    public ModelResponse {
        content = content == null ? "" : content;
        provider = provider == null ? "unknown" : provider;
        model = model == null ? "unknown" : model;
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
