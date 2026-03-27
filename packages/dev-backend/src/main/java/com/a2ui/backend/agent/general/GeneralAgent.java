package com.a2ui.backend.agent.general;

import reactor.core.publisher.Flux;

/**
 * General conversation agent interface.
 * Handles dialogue, routing, data queries, and A2A calls to A2UIAgent.
 */
public interface GeneralAgent {
    /**
     * Main chat entry point
     * @param userMessage User message
     * @param sessionId Session ID for conversation continuity
     * @return Streaming response strings
     */
    Flux<String> chat(String userMessage, Long sessionId);
}
