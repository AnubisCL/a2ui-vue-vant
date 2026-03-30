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
 * Global cache for chart tool results that should be rendered directly.
 *
 * When chart tools are called, they store their A2UI component results here.
 * The AgentCoordinator then retrieves these cached results and includes them
 * in the final output, bypassing LLM text generation.
 *
 * Uses sessionId as the key (derived from sessionId.hashCode()) to work across
 * different threads in reactive streams.
 */
@Slf4j
@Component
public class ChartResultCache {

    // Map of memory ID (sessionId hash) -> queue of chart messages
    private final Map<Long, ConcurrentLinkedQueue<ComponentMessage>> cache = new ConcurrentHashMap<>();

    /**
     * Get or create cache queue for a session
     */
    private ConcurrentLinkedQueue<ComponentMessage> getOrCreateQueue(Long memoryId) {
        return cache.computeIfAbsent(memoryId, k -> new ConcurrentLinkedQueue<>());
    }

    /**
     * Add a chart message to the cache for a specific session
     */
    public void addChartMessage(Long memoryId, ComponentMessage message) {
        if (memoryId == null) {
            log.warn("No memory ID provided, cannot cache chart message");
            return;
        }
        ConcurrentLinkedQueue<ComponentMessage> queue = getOrCreateQueue(memoryId);
        queue.offer(message);
        log.info("Chart message cached: componentId={}, memoryId={}", message.getComponentId(), memoryId);
    }

    /**
     * Get all cached chart messages and clear the cache for a specific session
     */
    public List<ComponentMessage> drainChartMessages(Long memoryId) {
        List<ComponentMessage> result = new ArrayList<>();
        if (memoryId == null) {
            log.warn("No memory ID provided, cannot drain chart messages");
            return result;
        }
        ConcurrentLinkedQueue<ComponentMessage> queue = cache.remove(memoryId);
        if (queue != null) {
            ComponentMessage msg;
            while ((msg = queue.poll()) != null) {
                result.add(msg);
            }
        }
        log.info("Drained {} chart messages from cache for memoryId: {}", result.size(), memoryId);
        return result;
    }

    /**
     * Check if there are cached chart messages for a specific session
     */
    public boolean hasChartMessages(Long memoryId) {
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
        log.info("All chart caches cleared");
    }
}
