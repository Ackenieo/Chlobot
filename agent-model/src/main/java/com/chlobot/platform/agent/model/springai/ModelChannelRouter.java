package com.chlobot.platform.agent.model.springai;

import com.chlobot.platform.agent.model.ModelRequest;
import com.chlobot.platform.agent.model.ModelResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ModelChannelRouter {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final AgentModelProperties properties;
    private final MeterRegistry meterRegistry;
    private final ModelChannel primary;
    private final ModelChannel mockFallback;

    public ModelChannelRouter(ChatModel chatModel, StreamingChatModel streamingChatModel,
                              AgentModelProperties properties, MeterRegistry meterRegistry) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.primary = new ModelChannel("primary", properties.getProvider(), properties.getName(),
                resourceName(properties.getProvider(), properties.getName(), "primary"), 100, false);
        this.mockFallback = new ModelChannel("mock-fallback", properties.getProvider(), properties.getName(),
                resourceName(properties.getProvider(), properties.getName(), "mock-fallback"), 0, true);
    }

    public ModelResponse chat(ModelRequest request) {
        if (!properties.hasApiKey() && properties.isMockFallback()) {
            increment("agent.model.fallbacks", mockFallback);
            return mockResponse(request, mockFallback);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Timer.Sample sample = Timer.start(meterRegistry);
            ChatResponse response = chatModel.call(request.prompt());
            sample.stop(timer("agent.model.call.latency", primary));
            increment("agent.model.calls", primary);
            stopWatch.stop();
            String content = content(response);
            Map<String, Object> metadata = metadata(response);
            metadata.put("latencyMs", stopWatch.getTotalTimeMillis());
            metadata.put("timeoutSeconds", timeoutSeconds(request));
            metadata.put("channel", primary.name());
            metadata.put("resourceName", primary.resourceName());
            recordTokenEstimate(content, metadata, primary);
            return new ModelResponse(content, primary.provider(), modelName(response), false, metadata);
        } catch (RuntimeException ex) {
            increment("agent.model.errors", primary);
            if (properties.isMockFallback()) {
                increment("agent.model.retries", primary);
                increment("agent.model.fallbacks", mockFallback);
                return mockResponse(request, mockFallback);
            }
            throw ex;
        }
    }

    public Flux<ModelResponse> stream(ModelRequest request) {
        if (!properties.hasApiKey() && properties.isMockFallback()) {
            increment("agent.model.fallbacks", mockFallback);
            return Flux.just(mockResponse(request, mockFallback));
        }
        long startedAt = System.nanoTime();
        AtomicBoolean firstTokenSeen = new AtomicBoolean(false);
        AtomicLong firstSseMs = new AtomicLong(-1);
        long timeoutSeconds = timeoutSeconds(request);
        return streamingChatModel.stream(request.prompt())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(response -> {
                    long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
                    if (firstTokenSeen.compareAndSet(false, true)) {
                        firstSseMs.set(elapsedMs);
                        timer("agent.model.first.token.latency", primary).record(Duration.ofMillis(elapsedMs));
                        timer("agent.model.first.sse.latency", primary).record(Duration.ofMillis(elapsedMs));
                    }
                    String content = content(response);
                    Map<String, Object> metadata = metadata(response);
                    metadata.put("streamLatencyMs", elapsedMs);
                    metadata.put("firstSseLatencyMs", firstSseMs.get());
                    metadata.put("timeoutSeconds", timeoutSeconds);
                    metadata.put("streamCallback", "chunk");
                    metadata.put("channel", primary.name());
                    metadata.put("resourceName", primary.resourceName());
                    recordTokenEstimate(content, metadata, primary);
                    increment("agent.model.stream.chunks", primary);
                    return new ModelResponse(content, primary.provider(), modelName(response), false, metadata);
                })
                .doOnError(error -> increment("agent.model.stream.errors", primary))
                .onErrorResume(error -> properties.isMockFallback(), error -> {
                    increment("agent.model.retries", primary);
                    increment("agent.model.fallbacks", mockFallback);
                    return Flux.just(mockResponse(request, mockFallback));
                });
    }

    public Map<String, Object> health() {
        return Map.of(
                "channels", List.of(primary, mockFallback),
                "active", properties.hasApiKey() ? primary.name() : mockFallback.name(),
                "sentinelResource", primary.resourceName(),
                "retryPolicy", "single Spring Retry-compatible fallback attempt for live channel failures",
                "fallbackEnabled", properties.isMockFallback()
        );
    }

    private ModelResponse mockResponse(ModelRequest request, ModelChannel channel) {
        increment("agent.model.calls", channel);
        String content = "[mock] " + request.prompt().getContents();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("fallback", "missing-api-key-or-live-channel-error");
        metadata.put("latencyMs", 0);
        metadata.put("timeoutSeconds", timeoutSeconds(request));
        metadata.put("channel", channel.name());
        metadata.put("resourceName", channel.resourceName());
        recordTokenEstimate(content, metadata, channel);
        return new ModelResponse(content, channel.provider(), channel.model(), true, metadata);
    }

    private Timer timer(String name, ModelChannel channel) {
        return Timer.builder(name)
                .tag("channel", channel.name())
                .tag("provider", channel.provider())
                .tag("model", channel.model())
                .tag("resource", channel.resourceName())
                .register(meterRegistry);
    }

    private void increment(String name, ModelChannel channel) {
        Counter.builder(name)
                .tag("channel", channel.name())
                .tag("provider", channel.provider())
                .tag("model", channel.model())
                .tag("resource", channel.resourceName())
                .register(meterRegistry)
                .increment();
    }

    private void recordTokenEstimate(String content, Map<String, Object> metadata, ModelChannel channel) {
        if (!metadata.containsKey("totalTokens")) {
            long estimate = estimateTokens(content);
            metadata.put("estimatedTokens", estimate);
            Counter.builder("agent.model.estimated.tokens")
                    .tag("channel", channel.name())
                    .tag("provider", channel.provider())
                    .tag("model", channel.model())
                    .tag("resource", channel.resourceName())
                    .register(meterRegistry)
                    .increment(estimate);
        }
    }

    private long estimateTokens(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return Math.max(1, (long) Math.ceil(content.length() / 4.0));
    }

    private String resourceName(String provider, String model, String channel) {
        return "chlobot:model:" + provider + ":" + model + ":" + channel;
    }

    private long timeoutSeconds(ModelRequest request) {
        if (request.timeout() != null && !request.timeout().isNegative() && !request.timeout().isZero()) {
            return request.timeout().toSeconds();
        }
        return properties.getTimeoutSeconds();
    }

    private String content(ChatResponse response) {
        return response.getResult() == null || response.getResult().getOutput() == null
                ? "" : response.getResult().getOutput().getText();
    }

    private String modelName(ChatResponse response) {
        if (response.getMetadata() != null && response.getMetadata().getModel() != null) {
            return response.getMetadata().getModel();
        }
        return properties.getName();
    }

    private Map<String, Object> metadata(ChatResponse response) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (response.getMetadata() != null) {
            metadata.put("id", response.getMetadata().getId());
            Usage usage = response.getMetadata().getUsage();
            if (usage != null) {
                metadata.put("promptTokens", usage.getPromptTokens());
                metadata.put("completionTokens", usage.getCompletionTokens());
                metadata.put("totalTokens", usage.getTotalTokens());
            }
        }
        return metadata;
    }
}
