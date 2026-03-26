package com.a2ui.backend.protocol.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.DataModelMessage;
import com.a2ui.backend.protocol.model.DeleteSurfaceMessage;
import com.a2ui.backend.protocol.model.SurfaceMessage;
import com.a2ui.backend.protocol.A2uiEncoder;

/**
 * Fluent builder for creating A2UI protocol messages.
 *
 * This builder provides convenient static methods to create properly
 * formatted JSON strings ready to be sent to the frontend.
 *
 * Usage examples:
 * <pre>
 * // Create a surface
 * String surface = A2uiMessageBuilder.createSurface("main", "Chat");
 *
 * // Append a text component
 * String text = A2uiMessageBuilder.appendComponent("main", "text1",
 *     ComponentBuilder.text("Hello World").build());
 *
 * // Update data model
 * String data = A2uiMessageBuilder.updateDataModel("main",
 *     Map.of("userName", "John", "balance", 100));
 * </pre>
 */
public class A2uiMessageBuilder {

    private A2uiMessageBuilder() {
        // Utility class - no instantiation
    }

    // ==================== Surface Operations ====================

    /**
     * Creates a surface creation message as JSON string.
     */
    public static String createSurface(String surfaceId, String name) {
        return A2uiEncoder.encode(SurfaceMessage.of(surfaceId, name));
    }

    /**
     * Creates a surface creation message with metadata as JSON string.
     */
    public static String createSurface(String surfaceId, String name, Map<String, Object> metadata) {
        return A2uiEncoder.encode(SurfaceMessage.of(surfaceId, name, metadata));
    }

    /**
     * Creates a delete surface message as JSON string.
     */
    public static String deleteSurface(String surfaceId) {
        return A2uiEncoder.encode(DeleteSurfaceMessage.of(surfaceId));
    }

    // ==================== Component Operations ====================

    /**
     * Creates an append component message as JSON string.
     */
    public static String appendComponent(String surfaceId, String componentId, ComponentSpec component) {
        return A2uiEncoder.encode(ComponentMessage.append(surfaceId, componentId, component));
    }

    /**
     * Creates an append component message from builder.
     */
    public static String appendComponent(String surfaceId, String componentId, ComponentBuilder builder) {
        return appendComponent(surfaceId, componentId, builder.build());
    }

    /**
     * Creates a prepend component message as JSON string.
     */
    public static String prependComponent(String surfaceId, String componentId, ComponentSpec component) {
        return A2uiEncoder.encode(ComponentMessage.prepend(surfaceId, componentId, component));
    }

    /**
     * Creates a prepend component message from builder.
     */
    public static String prependComponent(String surfaceId, String componentId, ComponentBuilder builder) {
        return prependComponent(surfaceId, componentId, builder.build());
    }

    /**
     * Creates a replace component message as JSON string.
     */
    public static String replaceComponent(String surfaceId, String componentId, ComponentSpec component) {
        return A2uiEncoder.encode(ComponentMessage.replace(surfaceId, componentId, component));
    }

    /**
     * Creates a replace component message from builder.
     */
    public static String replaceComponent(String surfaceId, String componentId, ComponentBuilder builder) {
        return replaceComponent(surfaceId, componentId, builder.build());
    }

    /**
     * Creates a component message with specified position as JSON string.
     */
    public static String updateComponent(String surfaceId, String componentId,
                                         String componentType, Map<String, Object> props) {
        ComponentSpec component = ComponentSpec.of(componentType, props);
        return A2uiEncoder.encode(ComponentMessage.of(surfaceId, componentId, component));
    }

    /**
     * Creates a component message with specified position.
     */
    public static String updateComponent(String surfaceId, String componentId,
                                         ComponentSpec component, String position) {
        return A2uiEncoder.encode(ComponentMessage.of(surfaceId, componentId, component, position));
    }

    // ==================== Data Model Operations ====================

    /**
     * Creates a data model update message as JSON string.
     */
    public static String updateDataModel(String surfaceId, Map<String, Object> data) {
        return A2uiEncoder.encode(DataModelMessage.of(surfaceId, data));
    }

    /**
     * Creates a data model replace message as JSON string.
     */
    public static String replaceDataModel(String surfaceId, Map<String, Object> data) {
        return A2uiEncoder.encode(DataModelMessage.replace(surfaceId, data));
    }

    // ==================== Convenience Methods ====================

    /**
     * Generates a unique component ID.
     */
    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a unique component ID with prefix.
     */
    public static String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== Quick Component Helpers ====================

    /**
     * Quick helper to append a text component.
     */
    public static String appendText(String surfaceId, String componentId, String content) {
        return appendComponent(surfaceId, componentId, ComponentBuilder.text(content));
    }

    /**
     * Quick helper to append a markdown component.
     */
    public static String appendMarkdown(String surfaceId, String componentId, String content) {
        return appendComponent(surfaceId, componentId, ComponentBuilder.markdown(content));
    }

    /**
     * Quick helper to append a card component.
     */
    public static String appendCard(String surfaceId, String componentId, String title, String content) {
        return appendComponent(surfaceId, componentId, ComponentBuilder.card(title, content));
    }

    /**
     * Quick helper to append a chart component.
     */
    public static String appendChart(String surfaceId, String componentId, Map<String, Object> echartsOption) {
        return appendComponent(surfaceId, componentId, ComponentBuilder.chart(echartsOption));
    }

    /**
     * Quick helper to append a divider.
     */
    public static String appendDivider(String surfaceId, String componentId) {
        return appendComponent(surfaceId, componentId, ComponentBuilder.divider());
    }

    // ==================== Message Sequence Helpers ====================

    /**
     * Builder for creating a sequence of messages.
     */
    public static class SequenceBuilder {
        private final List<Object> messages = new ArrayList<>();

        /**
         * Creates a new sequence builder.
         */
        public static SequenceBuilder create() {
            return new SequenceBuilder();
        }

        /**
         * Adds a surface creation message.
         */
        public SequenceBuilder surface(String surfaceId, String name) {
            messages.add(SurfaceMessage.of(surfaceId, name));
            return this;
        }

        /**
         * Adds a surface creation message with metadata.
         */
        public SequenceBuilder surface(String surfaceId, String name, Map<String, Object> metadata) {
            messages.add(SurfaceMessage.of(surfaceId, name, metadata));
            return this;
        }

        /**
         * Adds an append component message.
         */
        public SequenceBuilder append(String surfaceId, String componentId, ComponentSpec component) {
            messages.add(ComponentMessage.append(surfaceId, componentId, component));
            return this;
        }

        /**
         * Adds an append component message from builder.
         */
        public SequenceBuilder append(String surfaceId, String componentId, ComponentBuilder builder) {
            return append(surfaceId, componentId, builder.build());
        }

        /**
         * Adds a prepend component message.
         */
        public SequenceBuilder prepend(String surfaceId, String componentId, ComponentSpec component) {
            messages.add(ComponentMessage.prepend(surfaceId, componentId, component));
            return this;
        }

        /**
         * Adds a replace component message.
         */
        public SequenceBuilder replace(String surfaceId, String componentId, ComponentSpec component) {
            messages.add(ComponentMessage.replace(surfaceId, componentId, component));
            return this;
        }

        /**
         * Adds a data model update message.
         */
        public SequenceBuilder data(String surfaceId, Map<String, Object> data) {
            messages.add(DataModelMessage.of(surfaceId, data));
            return this;
        }

        /**
         * Adds a delete surface message.
         */
        public SequenceBuilder delete(String surfaceId) {
            messages.add(DeleteSurfaceMessage.of(surfaceId));
            return this;
        }

        /**
         * Adds a raw message object.
         */
        public SequenceBuilder add(Object message) {
            messages.add(message);
            return this;
        }

        /**
         * Builds the JSONL string.
         */
        public String build() {
            return A2uiEncoder.encodeAsJsonl(messages);
        }

        /**
         * Returns the list of messages.
         */
        public List<Object> getMessages() {
            return new ArrayList<>(messages);
        }
    }
}
