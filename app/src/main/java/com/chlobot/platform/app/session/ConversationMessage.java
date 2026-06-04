package com.chlobot.platform.app.session;

import java.time.Instant;
import java.util.Map;

public record ConversationMessage(Long id, String sessionId, String role, String content, Map<String, Object> metadata,
                                  Instant createdAt) {
}
