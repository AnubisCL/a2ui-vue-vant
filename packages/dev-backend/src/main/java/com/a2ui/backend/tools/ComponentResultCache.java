package com.a2ui.backend.tools;

import com.a2ui.backend.protocol.model.ComponentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Global cache for tool-generated component results that should be rendered directly.
 *
 * When tools (Chart, Form, etc.) are called, they store their A2UI component results here.
 * The AgentCoordinator then retrieves these cached results and includes them
 * in the final output, bypassing LLM text generation.
 *
 * Uses memoryId (derived from sessionId.hashCode()) as the key to work across
 * different threads in reactive streams.
 */
@Slf4j
@Component
public class ComponentResultCache {

    private final Map<Long, ConcurrentLinkedQueue<ComponentMessage>> cache = new ConcurrentHashMap<>();

    private ConcurrentLinkedQueue<ComponentMessage> getOrCreateQueue(Long memoryId) {
        return cache.computeIfAbsent(memoryId, k -> new ConcurrentLinkedQueue<>());
    }

    /**
     * Add a component message to the cache for a specific session
     */
    public void addComponent(Long memoryId, ComponentMessage message) {
        if (memoryId == null) {
            log.warn("No memory ID provided, cannot cache component message");
            return;
        }
        ConcurrentLinkedQueue<ComponentMessage> queue = getOrCreateQueue(memoryId);
        queue.offer(message);
        log.info("Component cached: componentId={}, memoryId={}", message.getComponentId(), memoryId);
    }

    /**
     * Get all cached component messages and clear the cache for a specific session
     */
    public List<ComponentMessage> drainComponents(Long memoryId) {
        List<ComponentMessage> result = new ArrayList<>();
        if (memoryId == null) {
            log.warn("No memory ID provided, cannot drain component messages");
            return result;
        }
        ConcurrentLinkedQueue<ComponentMessage> queue = cache.remove(memoryId);
        if (queue != null) {
            ComponentMessage msg;
            while ((msg = queue.poll()) != null) {
                result.add(msg);
            }
        }
        log.info("Drained {} component messages from cache for memoryId: {}", result.size(), memoryId);
        return result;
    }

    /**
     * Check if there are cached component messages for a specific session
     */
    public boolean hasComponents(Long memoryId) {
        if (memoryId == null) return false;
        ConcurrentLinkedQueue<ComponentMessage> queue = cache.get(memoryId);
        return queue != null && !queue.isEmpty();
    }

    /**
     * Clear all cached messages for a specific session
     */
    public void clear(Long memoryId) {
        if (memoryId != null) {
            cache.remove(memoryId);
        }
    }

    /**
     * Cleanup all cached messages (for testing or reset)
     */
    public void clearAll() {
        cache.clear();
        log.info("All component caches cleared");
    }
}
