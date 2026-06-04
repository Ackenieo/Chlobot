package com.chlobot.platform.app.task;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record CreateAgentTaskRequest(@NotBlank String sessionId, @NotBlank String input, String mode,
                                     Boolean requiresPlanConfirmation, Map<String, Object> budget) {
}
