package com.chlobot.platform.agent.model.springai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record StructuredOutput<T>(
        @NotBlank String schemaName,
        @NotNull T data,
        Map<String, Object> metadata) {

    public StructuredOutput {
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
