package com.a2ui.backend.protocol.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Specification for a UI component.
 *
 * This class represents a component definition with its type and properties.
 * The component type determines how the frontend should render it (e.g., Text, Chart, Card).
 *
 * Example JSON:
 * <pre>
 * {"type":"Text","props":{"content":"Hello World"}}
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentSpec {

    /**
     * The component type (e.g., "Text", "Chart", "Card", "Form", "Button").
     */
    @JsonProperty("type")
    private String type;

    /**
     * Component-specific properties.
     * The structure depends on the component type.
     */
    @JsonProperty("props")
    @Builder.Default
    private Map<String, Object> props = new HashMap<>();

    /**
     * Creates a component spec with the given type.
     */
    public static ComponentSpec of(String type) {
        return ComponentSpec.builder()
            .type(type)
            .build();
    }

    /**
     * Creates a component spec with type and initial props.
     */
    public static ComponentSpec of(String type, Map<String, Object> props) {
        return ComponentSpec.builder()
            .type(type)
            .props(props != null ? props : new HashMap<>())
            .build();
    }

    /**
     * Convenience method to add a property.
     */
    public ComponentSpec withProp(String key, Object value) {
        if (this.props == null) {
            this.props = new HashMap<>();
        }
        this.props.put(key, value);
        return this;
    }

    /**
     * Creates a Text component.
     */
    public static ComponentSpec text(String content) {
        return ComponentSpec.builder()
            .type("Text")
            .props(Map.of("content", content))
            .build();
    }

    /**
     * Creates a Text component with markdown support.
     */
    public static ComponentSpec markdown(String content) {
        return ComponentSpec.builder()
            .type("Text")
            .props(Map.of("content", content, "markdown", true))
            .build();
    }

    /**
     * Creates a Chart component with ECharts option.
     */
    public static ComponentSpec chart(Map<String, Object> echartsOption) {
        return ComponentSpec.builder()
            .type("Chart")
            .props(Map.of("option", echartsOption))
            .build();
    }

    /**
     * Creates a Card component.
     */
    public static ComponentSpec card(String title, String content) {
        Map<String, Object> props = new HashMap<>();
        if (title != null) props.put("title", title);
        props.put("content", content);
        return ComponentSpec.builder()
            .type("Card")
            .props(props)
            .build();
    }

    /**
     * Creates a Button component.
     */
    public static ComponentSpec button(String label, String action) {
        return ComponentSpec.builder()
            .type("Button")
            .props(Map.of("label", label, "action", action))
            .build();
    }

    /**
     * Creates a Divider component.
     */
    public static ComponentSpec divider() {
        return ComponentSpec.builder()
            .type("Divider")
            .build();
    }

    /**
     * Creates an Image component.
     */
    public static ComponentSpec image(String src, String alt) {
        return ComponentSpec.builder()
            .type("Image")
            .props(Map.of("src", src, "alt", alt != null ? alt : ""))
            .build();
    }
}
