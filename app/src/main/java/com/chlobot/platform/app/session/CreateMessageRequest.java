package com.chlobot.platform.app.session;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreateMessageRequest(@NotBlank String role, @NotBlank String content, Map<String, Object> metadata) {
}
