package com.a2ui.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for A2UI backend.
 *
 * Provides structured configuration for LLM providers, transport options,
 * and demo mode settings.
 */
@Data
@ConfigurationProperties(prefix = "a2ui")
public class A2uiProperties {

    private LlmConfig llm = new LlmConfig();
    private TransportConfig transport = new TransportConfig();
    private DemoConfig demo = new DemoConfig();
    private CorsConfig cors = new CorsConfig();

    /**
     * LLM provider configuration.
     */
    @Data
    public static class LlmConfig {
        /**
         * LLM provider type (openai, glm, azure-openai, etc.)
         */
        private String provider = "glm";

        /**
         * API key for the LLM provider
         */
        private String apiKey;

        /**
         * Model name to use (glm-4, glm-4-plus, glm-4-flash, etc.)
         */
        private String model = "glm-4";

        /**
         * Temperature for response generation (0.0 - 2.0)
         */
        private Double temperature = 0.7;

        /**
         * Maximum tokens in response
         */
        private Integer maxTokens = 4096;

        /**
         * Request timeout in milliseconds
         */
        private Integer timeout = 120000;

        /**
         * Base URL for API (optional, for custom endpoints)
         * Default for GLM: https://open.bigmodel.cn/api/coding/paas/v4
         */
        private String baseUrl;

        /**
         * Enable streaming mode
         */
        private boolean streaming = true;

        /**
         * Check if LLM is configured (API key is set)
         */
        public boolean isConfigured() {
            return apiKey != null && !apiKey.isEmpty() && !"your-api-key-here".equals(apiKey);
        }
    }

    /**
     * Transport layer configuration.
     */
    @Data
    public static class TransportConfig {
        private SseConfig sse = new SseConfig();
        private WebSocketConfig websocket = new WebSocketConfig();
    }

    /**
     * Server-Sent Events (HTTP Streaming) configuration.
     */
    @Data
    public static class SseConfig {
        /**
         * Enable SSE transport
         */
        private boolean enabled = true;

        /**
         * SSE endpoint path
         */
        private String endpoint = "/api/chat/stream";
    }

    /**
     * WebSocket configuration.
     */
    @Data
    public static class WebSocketConfig {
        /**
         * Enable WebSocket transport
         */
        private boolean enabled = true;

        /**
         * WebSocket endpoint path
         */
        private String endpoint = "/ws/chat";
    }

    /**
     * Demo mode configuration (when LLM is not available).
     */
    @Data
    public static class DemoConfig {
        /**
         * Enable demo mode
         */
        private boolean enabled = true;

        /**
         * Delay between streaming messages in milliseconds
         */
        private int streamingDelayMs = 100;

        /**
         * Enable demo charts
         */
        private boolean enableCharts = true;

        /**
         * Enable demo forms
         */
        private boolean enableForms = true;

        /**
         * Enable demo tables
         */
        private boolean enableTables = true;
    }

    /**
     * CORS configuration.
     */
    @Data
    public static class CorsConfig {
        /**
         * Allowed origins for CORS
         */
        private java.util.List<String> allowedOrigins = java.util.List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8081"
        );

        /**
         * Allowed HTTP methods
         */
        private java.util.List<String> allowedMethods = java.util.List.of(
            "GET", "POST", "OPTIONS"
        );

        /**
         * Allowed headers
         */
        private java.util.List<String> allowedHeaders = java.util.List.of("*");

        /**
         * Allow credentials
         */
        private boolean allowCredentials = true;

        /**
         * Max age in seconds
         */
        private long maxAge = 3600;
    }
}
