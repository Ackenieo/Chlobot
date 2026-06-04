package com.chlobot.platform.app.session;

import java.util.Map;

public record CreateSessionRequest(String title, Map<String, Object> metadata) {
}
