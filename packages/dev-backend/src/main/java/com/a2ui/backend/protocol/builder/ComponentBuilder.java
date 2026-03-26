package com.a2ui.backend.protocol.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.a2ui.backend.protocol.model.ComponentSpec;

/**
 * Fluent builder for creating component specifications.
 *
 * Usage examples:
 * <pre>
 * // Simple text
 * ComponentSpec text = ComponentBuilder.text("Hello World").build();
 *
 * // Chart with options
 * ComponentSpec chart = ComponentBuilder.chart()
 *     .withProp("option", chartOption)
 *     .withProp("height", 300)
 *     .build();
 *
 * // Custom component
 * ComponentSpec custom = ComponentBuilder.create("CustomWidget")
 *     .withProp("title", "My Widget")
 *     .withProp("data", myData)
 *     .build();
 * </pre>
 */
public class ComponentBuilder {

    private String type;
    private Map<String, Object> props;

    private ComponentBuilder(String type) {
        this.type = type;
        this.props = new HashMap<>();
    }

    private ComponentBuilder(String type, Map<String, Object> initialProps) {
        this.type = type;
        this.props = new HashMap<>(initialProps != null ? initialProps : Map.of());
    }

    /**
     * Creates a builder for a custom component type.
     */
    public static ComponentBuilder create(String type) {
        return new ComponentBuilder(type);
    }

    /**
     * Creates a Text component builder.
     */
    public static ComponentBuilder text(String content) {
        return new ComponentBuilder("Text", Map.of("content", content));
    }

    /**
     * Creates a Text component with markdown support.
     */
    public static ComponentBuilder markdown(String content) {
        return new ComponentBuilder("Text", Map.of("content", content, "markdown", true));
    }

    /**
     * Creates a Chart component builder (for ECharts).
     */
    public static ComponentBuilder chart() {
        return new ComponentBuilder("Chart");
    }

    /**
     * Creates a Chart component builder with initial option.
     */
    public static ComponentBuilder chart(Map<String, Object> echartsOption) {
        return new ComponentBuilder("Chart", Map.of("option", echartsOption));
    }

    /**
     * Creates a Card component builder.
     */
    public static ComponentBuilder card(String title, String content) {
        Map<String, Object> props = new HashMap<>();
        if (title != null && !title.isEmpty()) {
            props.put("title", title);
        }
        props.put("content", content);
        return new ComponentBuilder("Card", props);
    }

    /**
     * Creates a Card component builder without title.
     */
    public static ComponentBuilder card(String content) {
        return card(null, content);
    }

    /**
     * Creates a Button component builder.
     */
    public static ComponentBuilder button(String label) {
        return new ComponentBuilder("Button", Map.of("label", label));
    }

    /**
     * Creates a Button component builder with action.
     */
    public static ComponentBuilder button(String label, String action) {
        return new ComponentBuilder("Button", Map.of("label", label, "action", action));
    }

    /**
     * Creates a Form component builder.
     */
    public static ComponentBuilder form() {
        return new ComponentBuilder("Form");
    }

    /**
     * Creates a Form component builder with fields.
     */
    public static ComponentBuilder form(List<FormField> fields) {
        List<Map<String, Object>> fieldList = new ArrayList<>();
        for (FormField field : fields) {
            fieldList.add(field.toMap());
        }
        return new ComponentBuilder("Form", Map.of("fields", fieldList));
    }

    /**
     * Creates an Image component builder.
     */
    public static ComponentBuilder image(String src) {
        return new ComponentBuilder("Image", Map.of("src", src));
    }

    /**
     * Creates an Image component builder with alt text.
     */
    public static ComponentBuilder image(String src, String alt) {
        return new ComponentBuilder("Image", Map.of("src", src, "alt", alt != null ? alt : ""));
    }

    /**
     * Creates a Divider component.
     */
    public static ComponentBuilder divider() {
        return new ComponentBuilder("Divider");
    }

    /**
     * Creates a Spacer component with specified height.
     */
    public static ComponentBuilder spacer(int height) {
        return new ComponentBuilder("Spacer", Map.of("height", height));
    }

    /**
     * Creates a Tabs component builder.
     */
    public static ComponentBuilder tabs() {
        return new ComponentBuilder("Tabs");
    }

    /**
     * Creates a Table component builder.
     */
    public static ComponentBuilder table() {
        return new ComponentBuilder("Table");
    }

    /**
     * Creates a List component builder.
     */
    public static ComponentBuilder list() {
        return new ComponentBuilder("List");
    }

    /**
     * Adds a property to the component.
     */
    public ComponentBuilder withProp(String key, Object value) {
        this.props.put(key, value);
        return this;
    }

    /**
     * Adds multiple properties to the component.
     */
    public ComponentBuilder withProps(Map<String, Object> additionalProps) {
        if (additionalProps != null) {
            this.props.putAll(additionalProps);
        }
        return this;
    }

    /**
     * Sets the component ID (for reference).
     * Note: This adds it as a prop, actual componentId is set in ComponentMessage.
     */
    public ComponentBuilder withId(String id) {
        this.props.put("id", id);
        return this;
    }

    /**
     * Adds a CSS class to the component.
     */
    public ComponentBuilder withClass(String className) {
        this.props.put("className", className);
        return this;
    }

    /**
     * Adds inline styles to the component.
     */
    public ComponentBuilder withStyle(Map<String, Object> style) {
        this.props.put("style", style);
        return this;
    }

    /**
     * Sets the component to be visible or hidden.
     */
    public ComponentBuilder visible(boolean visible) {
        this.props.put("visible", visible);
        return this;
    }

    /**
     * Builds the final ComponentSpec.
     */
    public ComponentSpec build() {
        return ComponentSpec.builder()
            .type(this.type)
            .props(new HashMap<>(this.props))
            .build();
    }

    /**
     * Represents a form field for Form component.
     */
    public static class FormField {
        private String name;
        private String label;
        private String type;
        private Object defaultValue;
        private boolean required;
        private Map<String, Object> options;

        public FormField(String name, String label, String type) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = false;
            this.options = new HashMap<>();
        }

        public static FormField text(String name, String label) {
            return new FormField(name, label, "text");
        }

        public static FormField number(String name, String label) {
            return new FormField(name, label, "number");
        }

        public static FormField select(String name, String label) {
            return new FormField(name, label, "select");
        }

        public static FormField textarea(String name, String label) {
            return new FormField(name, label, "textarea");
        }

        public static FormField checkbox(String name, String label) {
            return new FormField(name, label, "checkbox");
        }

        public static FormField date(String name, String label) {
            return new FormField(name, label, "date");
        }

        public FormField required() {
            this.required = true;
            return this;
        }

        public FormField defaultValue(Object value) {
            this.defaultValue = value;
            return this;
        }

        public FormField placeholder(String placeholder) {
            return option("placeholder", placeholder);
        }

        public FormField option(String key, Object value) {
            if (this.options == null) {
                this.options = new HashMap<>();
            }
            this.options.put(key, value);
            return this;
        }

        public FormField options(List<Map<String, Object>> selectOptions) {
            return option("options", selectOptions);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("label", label);
            map.put("type", type);
            map.put("required", required);
            if (defaultValue != null) {
                map.put("defaultValue", defaultValue);
            }
            if (options != null && !options.isEmpty()) {
                map.put("options", options);
            }
            return map;
        }
    }
}
