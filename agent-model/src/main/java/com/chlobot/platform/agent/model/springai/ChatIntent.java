package com.chlobot.platform.agent.model.springai;

import jakarta.validation.constraints.NotBlank;

public record ChatIntent(@NotBlank String intent, String summary) {
}
