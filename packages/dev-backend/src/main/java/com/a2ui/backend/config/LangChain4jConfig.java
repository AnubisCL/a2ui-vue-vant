package com.a2ui.backend.config;

import com.a2ui.backend.agent.a2ui.A2UIAgent;
import com.a2ui.backend.agent.general.GeneralAgent;
import com.a2ui.backend.agent.general.GeneralAgentTools;
import com.a2ui.backend.llm.A2uiAssistant;
import com.a2ui.backend.tools.ChartGeneratorTool;
import com.a2ui.backend.tools.DataQueryTool;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * LangChain4j configuration for LLM integration.
 *
 * Creates beans for chat and streaming models when API key is configured.
 * Supports GLM (智谱) and OpenAI compatible APIs.
 * Falls back to demo mode when LLM is not available.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(A2uiProperties.class)
public class LangChain4jConfig {

    private final A2uiProperties a2uiProperties;
    private final DataQueryTool dataQueryTool;
    private final ChartGeneratorTool chartGeneratorTool;

    public LangChain4jConfig(A2uiProperties a2uiProperties,
                             DataQueryTool dataQueryTool,
                             ChartGeneratorTool chartGeneratorTool) {
        this.a2uiProperties = a2uiProperties;
        this.dataQueryTool = dataQueryTool;
        this.chartGeneratorTool = chartGeneratorTool;
    }

    /**
     * Creates an A2uiAssistant bean using AiServices builder.
     * This provides a streaming-capable assistant with the A2UI system prompt.
     */
    @Bean
    @ConditionalOnProperty(prefix = "a2ui.llm", name = "api-key")
    public A2uiAssistant a2uiAssistant() {
        A2uiProperties.LlmConfig llmConfig = a2uiProperties.getLlm();

        String baseUrl = llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()
            ? llmConfig.getBaseUrl()
            : "https://open.bigmodel.cn/api/coding/paas/v4";

        Duration timeout = Duration.ofMillis(llmConfig.getTimeout() != null ? llmConfig.getTimeout() : 120000);
        Double temperature = llmConfig.getTemperature() != null ? llmConfig.getTemperature() : 0.7;

        log.info("Creating A2uiAssistant with model: {}, baseUrl: {}", llmConfig.getModel(), baseUrl);

        // Build HTTP client with timeout
        JdkHttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder()
            .connectTimeout(timeout)
            .readTimeout(timeout);

        // Build streaming model
        StreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
            .httpClientBuilder(httpClientBuilder)
            .apiKey(llmConfig.getApiKey())
            .modelName(llmConfig.getModel())
            .baseUrl(baseUrl)
            .temperature(temperature)
            .logRequests(true)
            .logResponses(true)
            .build();

        log.info("Creating A2uiAssistant with streaming model and tools: DataQueryTool, ChartGeneratorTool");

        return AiServices.builder(A2uiAssistant.class)
            .streamingChatModel(streamingModel)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
            .tools(dataQueryTool, chartGeneratorTool)
            .build();
    }

    /**
     * Creates a helper StreamingChatModel for agents
     */
    private StreamingChatModel createStreamingChatModel() {
        A2uiProperties.LlmConfig llmConfig = a2uiProperties.getLlm();

        String baseUrl = llmConfig.getBaseUrl() != null && !llmConfig.getBaseUrl().isEmpty()
            ? llmConfig.getBaseUrl()
            : "https://open.bigmodel.cn/api/coding/paas/v4";

        Duration timeout = Duration.ofMillis(llmConfig.getTimeout() != null ? llmConfig.getTimeout() : 120000);
        Double temperature = llmConfig.getTemperature() != null ? llmConfig.getTemperature() : 0.7;

        log.info("Creating StreamingChatModel: {}, baseUrl: {}", llmConfig.getModel(), baseUrl);

        JdkHttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder()
            .connectTimeout(timeout)
            .readTimeout(timeout);

        return OpenAiStreamingChatModel.builder()
            .httpClientBuilder(httpClientBuilder)
            .apiKey(llmConfig.getApiKey())
            .modelName(llmConfig.getModel())
            .baseUrl(baseUrl)
            .temperature(temperature)
            .logRequests(true)
            .logResponses(true)
            .build();
    }

    /**
     * Creates GeneralAgentTools bean aggregating all tools for GeneralAgent
     */
    @Bean
    public GeneralAgentTools generalAgentTools() {
        return new GeneralAgentTools(dataQueryTool, chartGeneratorTool);
    }

    /**
     * Creates A2UIAgent bean using AiServices builder
     */
    @Bean
    public A2UIAgent a2uiAgent() {
        StreamingChatModel model = createStreamingChatModel();

        return AiServices.builder(A2UIAgent.class)
            .streamingChatModel(model)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(50))
            .build();
    }

    /**
     * Creates GeneralAgent bean using AiServices builder with tools.
     * Note: Pass individual tool instances directly, not a wrapper class.
     */
    @Bean
    public GeneralAgent generalAgent() {
        StreamingChatModel model = createStreamingChatModel();

        log.info("Creating GeneralAgent with tools: DataQueryTool, ChartGeneratorTool");

        return AiServices.builder(GeneralAgent.class)
            .streamingChatModel(model)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
            .tools(dataQueryTool, chartGeneratorTool)
            .build();
    }

    /**
     * Creates a WebClient bean for custom HTTP calls if needed.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
