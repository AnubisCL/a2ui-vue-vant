package com.a2ui.backend.transport.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket configuration for A2UI backend.
 *
 * Registers the A2UI WebSocket handler at the configured endpoint.
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final A2uiWebSocketHandler a2uiWebSocketHandler;

    @Value("${a2ui.transport.websocket.endpoint:/ws/chat}")
    private String websocketEndpoint;

    @Value("${a2ui.transport.websocket.enabled:true}")
    private boolean websocketEnabled;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (!websocketEnabled) {
            log.info("WebSocket transport is disabled");
            return;
        }

        log.info("Registering WebSocket handler at: {}", websocketEndpoint);

        registry.addHandler(a2uiWebSocketHandler, websocketEndpoint)
            .setAllowedOrigins("*");
    }
}
