package com.a2ui.backend.agent.general;

import reactor.core.publisher.Flux;

/**
 * General conversation agent interface.
 *
 * Handles user conversations with LLM, delegates data queries and
 * UI generation to appropriate tools or agents.
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
