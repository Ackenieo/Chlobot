package com.chlobot.platform.agent.model.springai;

public record ModelChannel(String name, String provider, String model, String resourceName, int priority, boolean fallback) {
}
