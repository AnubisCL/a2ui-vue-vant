package com.a2ui.backend.agent.a2ui;

import com.a2ui.backend.agent.dto.A2UIRequest;
import reactor.core.publisher.Flux;

/**
 * A2UI component generation agent interface.
 * This interface defines the contract for A2UI component generation.
 *
 * Implementations can use LangChain4j declarative agents, Spring beans,
 * or programmatic agent building.
 */
public interface A2UIAgent {
    /**
     * Generate A2UI component synchronously
     * @param request A2UI request with intent, data, displayType, etc.
     * @return A2UI JSON string
     */
    String generate(A2UIRequest request);

    /**
     * Generate A2UI component with streaming
     * @param request A2UI request
     * @return Streaming A2UI JSON
     */
    Flux<String> generateStream(A2UIRequest request);
}
