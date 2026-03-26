package com.a2ui.backend.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j configuration for LLM integration.
 *
 * Creates beans for chat and streaming models when API key is configured.
 * Falls back to demo mode when LLM is not available.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(A2uiProperties.class)
public class LangChain4jConfig {

    private final A2uiProperties a2uiProperties;

    /**
     * Creates a ChatLanguageModel bean for synchronous LLM calls.
     * Only created when a2ui.llm.api-key is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "a2ui.llm", name = "api-key")
    public ChatLanguageModel chatLanguageModel() {
        A2uiProperties.LlmConfig llmConfig = a2uiProperties.getLlm();

        log.info("Configuring ChatLanguageModel with provider: {}, model: {}",
            llmConfig.getProvider(), llmConfig.getModel());

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
            .apiKey(llmConfig.getApiKey())
            .modelName(llmConfig.getModel())
            .temperature(llmConfig.getTemperature())
            .maxTokens(llmConfig.getMaxTokens());

        if (llmConfig.getTimeout() != null) {
            builder.timeout(Duration.ofMillis(llmConfig.getTimeout()));
        }

        if (llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()) {
            builder.baseUrl(llmConfig.getBaseUrl());
        }

        log.info("ChatLanguageModel configured successfully");
        return builder.build();
    }

    /**
     * Creates a StreamingChatModel bean for streaming LLM responses.
     * Only created when a2ui.llm.api-key is configured.
     */
    @Bean
    @ConditionalOnProperty(prefix = "a2ui.llm", name = "api-key")
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        A2uiProperties.LlmConfig llmConfig = a2uiProperties.getLlm();

        log.info("Configuring StreamingChatModel with provider: {}, model: {}",
            llmConfig.getProvider(), llmConfig.getModel());

        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
            .apiKey(llmConfig.getApiKey())
            .modelName(llmConfig.getModel())
            .temperature(llmConfig.getTemperature());

        if (llmConfig.getTimeout() != null) {
            builder.timeout(Duration.ofMillis(llmConfig.getTimeout()));
        }

        if (llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()) {
            builder.baseUrl(llmConfig.getBaseUrl());
        }

        log.info("StreamingChatModel configured successfully");
        return builder.build();
    }
}
