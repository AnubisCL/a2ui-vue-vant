package com.a2ui.backend.protocol.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Message to add, update, or replace a component in a surface.
 *
 * Example JSON:
 * <pre>
 * {"type":"component","surfaceId":"main","componentId":"text1","component":{"type":"Text","props":{"content":"Hello"}}}
 * </pre>
 *
 * Position values:
 * - "append" (default): Add component at the end
 * - "prepend": Add component at the beginning
 * - "replace": Replace existing component with same ID
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentMessage extends A2uiMessage {

    public static final String TYPE = "component";

    public static final String POSITION_APPEND = "append";
    public static final String POSITION_PREPEND = "prepend";
    public static final String POSITION_REPLACE = "replace";

    /**
     * Unique identifier for the component within the surface.
     */
    @JsonProperty("componentId")
    private String componentId;

    /**
     * The component specification.
     */
    @JsonProperty("component")
    private ComponentSpec component;

    /**
     * Position strategy: "append", "prepend", or "replace".
     * Default is "append".
     */
    @JsonProperty("position")
    private String position = POSITION_APPEND;

    /**
     * Creates an append component message.
     */
    public static ComponentMessage append(String surfaceId, String componentId, ComponentSpec component) {
        return ComponentMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .componentId(componentId)
            .component(component)
            .position(POSITION_APPEND)
            .build();
    }

    /**
     * Creates a prepend component message.
     */
    public static ComponentMessage prepend(String surfaceId, String componentId, ComponentSpec component) {
        return ComponentMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .componentId(componentId)
            .component(component)
            .position(POSITION_PREPEND)
            .build();
    }

    /**
     * Creates a replace component message.
     */
    public static ComponentMessage replace(String surfaceId, String componentId, ComponentSpec component) {
        return ComponentMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .componentId(componentId)
            .component(component)
            .position(POSITION_REPLACE)
            .build();
    }

    /**
     * Creates a component message with default (append) position.
     */
    public static ComponentMessage of(String surfaceId, String componentId, ComponentSpec component) {
        return ComponentMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .componentId(componentId)
            .component(component)
            .build();
    }

    /**
     * Creates a component message with explicit position.
     */
    public static ComponentMessage of(String surfaceId, String componentId, ComponentSpec component, String position) {
        return ComponentMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .componentId(componentId)
            .component(component)
            .position(position)
            .build();
    }

    @Override
    public boolean isValid() {
        return super.isValid()
            && componentId != null && !componentId.isEmpty()
            && component != null
            && component.getType() != null;
    }
}
