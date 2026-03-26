package com.a2ui.backend.transport.http;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.a2ui.backend.config.A2uiProperties;
import com.a2ui.backend.llm.StreamingHandler;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * HTTP Streaming controller using Server-Sent Events (SSE) for A2UI protocol.
 *
 * Provides a streaming endpoint for real-time A2UI message delivery.
 * Uses StreamingHandler for LLM or demo responses.
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "${a2ui.cors.allowed-origins:http://localhost:5173}", allowCredentials = "true")
public class A2uiHttpController {

    private final A2uiProperties a2uiProperties;
    private final StreamingHandler streamingHandler;

    /**
     * SSE endpoint for streaming A2UI messages.
     *
     * @param message The user's message
     * @param sessionId Optional session ID for conversation continuity
     * @return Flux of ServerSentEvent containing A2UI JSONL messages
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
        @RequestParam String message,
        @RequestParam(required = false) String sessionId
    ) {
        if (!a2uiProperties.getTransport().getSse().isEnabled()) {
            return Flux.just(createErrorEvent("SSE transport is disabled"));
        }

        String effectiveSessionId = sessionId != null ? sessionId : generateSessionId();
        log.info("SSE stream request: sessionId={}, message={}, llmAvailable={}",
            effectiveSessionId, message, streamingHandler.isLlmAvailable());

        return streamingHandler.generateResponse(message, effectiveSessionId)
            .index((index, a2uiMessage) -> {
                String json = A2uiEncoder.encode(a2uiMessage);
                log.debug("Sending SSE event #{}: {}", index, json);
                return ServerSentEvent.<String>builder()
                    .id(String.valueOf(index))
                    .event("message")
                    .data(json)
                    .build();
            });
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        return Mono.just(Map.of(
            "status", "UP",
            "llm", streamingHandler.isLlmAvailable() ? "configured" : "demo-mode",
            "transport", Map.of(
                "sse", a2uiProperties.getTransport().getSse().isEnabled() ? "enabled" : "disabled",
                "websocket", a2uiProperties.getTransport().getWebsocket().isEnabled() ? "enabled" : "disabled"
            )
        ));
    }

    /**
     * Status endpoint with detailed configuration info.
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> status() {
        return Mono.just(Map.of(
            "status", "UP",
            "llm", Map.of(
                "available", streamingHandler.isLlmAvailable(),
                "provider", a2uiProperties.getLlm().getProvider(),
                "model", a2uiProperties.getLlm().getModel()
            ),
            "transport", Map.of(
                "sse", Map.of(
                    "enabled", a2uiProperties.getTransport().getSse().isEnabled(),
                    "endpoint", a2uiProperties.getTransport().getSse().getEndpoint()
                ),
                "websocket", Map.of(
                    "enabled", a2uiProperties.getTransport().getWebsocket().isEnabled(),
                    "endpoint", a2uiProperties.getTransport().getWebsocket().getEndpoint()
                )
            ),
            "demo", Map.of(
                "enabled", a2uiProperties.getDemo().isEnabled(),
                "streamingDelayMs", a2uiProperties.getDemo().getStreamingDelayMs()
            )
        ));
    }

    /**
     * Generates a unique session ID.
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Creates an error SSE event.
     */
    private ServerSentEvent<String> createErrorEvent(String errorMessage) {
        return ServerSentEvent.<String>builder()
            .event("error")
            .data("{\"error\":\"" + errorMessage + "\"}")
            .build();
    }
}
