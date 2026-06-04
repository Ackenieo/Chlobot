package com.chlobot.platform.agent.model.springai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.model")
public class AgentModelProperties {

    private String provider = "deepseek";
    private String name = "deepseek-chat";
    private String apiKey = "";
    private String baseUrl = "https://api.deepseek.com/v1";
    private boolean mockFallback = true;
    private long timeoutSeconds = 60;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isMockFallback() {
        return mockFallback;
    }

    public void setMockFallback(boolean mockFallback) {
        this.mockFallback = mockFallback;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank() && !"mock-api-key".equals(apiKey);
    }
}
