package com.a2ui.backend.demo;

import reactor.core.publisher.Flux;

/**
 * Interface for demo scenarios that showcase A2UI protocol capabilities.
 *
 * Each scenario represents a specific use case (data query, order management, etc.)
 * and can determine if it matches a user's message and generate appropriate responses.
 */
public interface DemoScenario {

    /**
     * Get the name of this scenario.
     *
     * @return Scenario name for display/identification
     */
    String getName();

    /**
     * Get a description of what this scenario demonstrates.
     *
     * @return Scenario description
     */
    String getDescription();

    /**
     * Check if this scenario should handle the given user message.
     *
     * @param userMessage The user's input message
     * @return true if this scenario should handle the message
     */
    boolean matches(String userMessage);

    /**
     * Execute the scenario and generate A2UI protocol messages.
     *
     * The returned Flux emits JSON strings, each being a valid A2UI protocol message
     * that can be sent directly to the frontend.
     *
     * @param userMessage The user's input message
     * @param sessionId   The session ID for state management
     * @return Flux of A2UI protocol JSON messages
     */
    Flux<String> execute(String userMessage, String sessionId);
}
