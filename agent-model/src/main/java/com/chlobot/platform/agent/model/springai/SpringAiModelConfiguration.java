package com.chlobot.platform.agent.model.springai;

import com.chlobot.platform.agent.model.ModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.Validator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;

import io.micrometer.observation.ObservationRegistry;

@Configuration
@EnableConfigurationProperties(AgentModelProperties.class)
public class SpringAiModelConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "springAiChatModel")
    public ChatModel springAiChatModel(AgentModelProperties properties) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.hasApiKey() ? properties.getApiKey() : "mock-api-key")
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(properties.getName())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .toolCallingManager(ToolCallingManager.builder().build())
                .retryTemplate(RetryTemplate.defaultInstance())
                .observationRegistry(ObservationRegistry.NOOP)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public StructuredOutputHelper structuredOutputHelper(ObjectMapper objectMapper, Validator validator) {
        return new StructuredOutputHelper(objectMapper, validator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelChannelRouter modelChannelRouter(ChatModel chatModel, StreamingChatModel streamingChatModel,
                                                 AgentModelProperties properties, MeterRegistry meterRegistry) {
        return new ModelChannelRouter(chatModel, streamingChatModel, properties, meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelClient modelClient(ModelChannelRouter modelChannelRouter) {
        return new SpringAiModelClient(modelChannelRouter);
    }
}
