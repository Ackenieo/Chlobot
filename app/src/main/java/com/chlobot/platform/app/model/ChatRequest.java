package com.chlobot.platform.app.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(@NotBlank String message, String system, List<String> knowledgeBaseIds) {
}
