package com.a2ui.backend.transport.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.a2ui.backend.config.A2uiProperties;
import com.a2ui.backend.llm.StreamingHandler;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

/**
 * WebSocket handler for A2UI protocol messages.
 *
 * Handles bidirectional communication with A2UI frontend clients.
 * Uses StreamingHandler for LLM or demo responses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class A2uiWebSocketHandler extends TextWebSocketHandler {

    private final A2uiProperties a2uiProperties;
    private final StreamingHandler streamingHandler;
    private final ObjectMapper objectMapper = A2uiEncoder.getMapper();

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Disposable> activeStreams = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!a2uiProperties.getTransport().getWebsocket().isEnabled()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("WebSocket transport is disabled"));
            return;
        }

        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: sessionId={}, uri={}, llmAvailable={}",
            session.getId(), session.getUri(), streamingHandler.isLlmAvailable());

        // Send welcome message
        String welcomeMessage = """
            {"type":"component","surfaceId":"main","componentId":"welcome","component":{"type":"Text","props":{"content":"Welcome to A2UI! Send a message to get started.","markdown":true}}}
            """;
        sendMessage(session, welcomeMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: sessionId={}, payload={}",
            session.getId(), payload);

        try {
            // Parse incoming message
            JsonNode jsonNode = objectMapper.readTree(payload);
            String userMessage = jsonNode.has("message")
                ? jsonNode.get("message").asText()
                : payload;

            // Cancel any existing stream for this session
            cancelActiveStream(session.getId());

            // Generate and stream A2UI response
            Disposable subscription = streamingHandler.generateResponse(userMessage, session.getId())
                .subscribe(
                    a2uiMessage -> {
                        try {
                            String json = A2uiEncoder.encode(a2uiMessage);
                            sendMessage(session, json);
                        } catch (Exception e) {
                            log.error("Error sending message: {}", e.getMessage());
                        }
                    },
                    error -> {
                        log.error("Error in stream: {}", error.getMessage());
                        sendErrorMessage(session, "Error processing message: " + error.getMessage());
                        activeStreams.remove(session.getId());
                    },
                    () -> {
                        log.debug("Stream completed for session: {}", session.getId());
                        activeStreams.remove(session.getId());
                    }
                );

            activeStreams.put(session.getId(), subscription);

        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
            sendErrorMessage(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        cancelActiveStream(session.getId());
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: sessionId={}, status={}",
            session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: sessionId={}, error={}",
            session.getId(), exception.getMessage(), exception);
        cancelActiveStream(session.getId());
        sessions.remove(session.getId());
    }

    /**
     * Cancels any active stream for the given session.
     */
    private void cancelActiveStream(String sessionId) {
        Disposable subscription = activeStreams.remove(sessionId);
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.debug("Cancelled active stream for session: {}", sessionId);
        }
    }

    /**
     * Sends an error message to the client.
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            String errorText = """
                {"type":"component","surfaceId":"main","componentId":"error","component":{"type":"Text","props":{"content":"**Error:** %s","markdown":true}}}
                """.formatted(errorMessage);
            sendMessage(session, errorText);
        } catch (Exception e) {
            log.error("Failed to send error message: {}", e.getMessage());
        }
    }

    /**
     * Sends a text message to a WebSocket session.
     */
    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**
     * Broadcasts a message to all connected sessions.
     */
    public void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("Failed to broadcast to session {}: {}",
                    session.getId(), e.getMessage());
            }
        });
    }

    /**
     * Returns the number of active sessions.
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * Returns all active sessions.
     */
    public Map<String, WebSocketSession> getSessions() {
        return new ConcurrentHashMap<>(sessions);
    }

    /**
     * Returns whether LLM is available.
     */
    public boolean isLlmAvailable() {
        return streamingHandler.isLlmAvailable();
    }
}
