package com.a2ui.backend.tools;

import com.a2ui.backend.protocol.model.ComponentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-local cache for chart tool results that should be rendered directly.
 *
 * When chart tools are called, they store their A2UI component results here.
 * The AgentCoordinator then retrieves these cached results and includes them
 * in the final output, bypassing LLM text generation.
 *
 * Uses ThreadLocal to ensure each request has its own cache.
 */
@Slf4j
@Component
public class ChartResultCache {

    private final ThreadLocal<ConcurrentLinkedQueue<ComponentMessage>> threadLocalCache = ThreadLocal.withInitial(ConcurrentLinkedQueue::new);

    /**
     * Add a chart message to the cache for the current thread
     */
    public void addChartMessage(ComponentMessage message) {
        threadLocalCache.get().offer(message);
        log.debug("Chart message cached: componentId={}", message.getComponentId());
    }

    /**
     * Get all cached chart messages and clear the cache for the current thread
     */
    public List<ComponentMessage> drainChartMessages() {
        List<ComponentMessage> result = new ArrayList<>();
        ConcurrentLinkedQueue<ComponentMessage> queue = threadLocalCache.get();
        ComponentMessage msg;
        while ((msg = queue.poll()) != null) {
            result.add(msg);
        }
        log.debug("Drained {} chart messages from cache", result.size());
        return result;
    }

    /**
     * Check if there are cached chart messages for the current thread
     */
    public boolean hasChartMessages() {
        return !threadLocalCache.get().isEmpty();
    }

    /**
     * Clear all cached messages for the current thread
     */
    public void clear() {
        threadLocalCache.get().clear();
    }
}
