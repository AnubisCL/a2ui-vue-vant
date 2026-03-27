package com.a2ui.backend.agent.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class A2UIComponent {
    private final String type;
    private final String surfaceId;
    private final String componentId;
    private final String name;
    private final ComponentSpec component;
    private final String position;

    @JsonCreator
    public A2UIComponent(
            @JsonProperty("type") String type,
            @JsonProperty("surfaceId") String surfaceId,
            @JsonProperty("componentId") String componentId,
            @JsonProperty("name") String name,
            @JsonProperty("component") ComponentSpec component,
            @JsonProperty("position") String position) {
        this.type = type;
        this.surfaceId = surfaceId;
        this.componentId = componentId;
        this.name = name;
        this.component = component;
        this.position = position;
    }

    private A2UIComponent(Builder builder) {
        this.type = builder.type;
        this.surfaceId = builder.surfaceId;
        this.componentId = builder.componentId;
        this.name = builder.name;
        this.component = builder.component;
        this.position = builder.position;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getType() { return type; }
    public String getSurfaceId() { return surfaceId; }
    public String getComponentId() { return componentId; }
    public String getName() { return name; }
    public ComponentSpec getComponent() { return component; }
    public String getPosition() { return position; }

    @Override
    public String toString() {
        return "A2UIComponent(type=" + type + ", surfaceId=" + surfaceId +
                ", componentId=" + componentId + ", name=" + name +
                ", component=" + component + ", position=" + position + ")";
    }

    public static class Builder {
        private String type;
        private String surfaceId;
        private String componentId;
        private String name;
        private ComponentSpec component;
        private String position;

        public Builder type(String type) { this.type = type; return this; }
        public Builder surfaceId(String surfaceId) { this.surfaceId = surfaceId; return this; }
        public Builder componentId(String componentId) { this.componentId = componentId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder component(ComponentSpec component) { this.component = component; return this; }
        public Builder position(String position) { this.position = position; return this; }

        public A2UIComponent build() {
            return new A2UIComponent(this);
        }
    }

    public static final class ComponentSpec {
        private final String type;
        private final Map<String, Object> props;

        @JsonCreator
        public ComponentSpec(
                @JsonProperty("type") String type,
                @JsonProperty("props") Map<String, Object> props) {
            this.type = type;
            this.props = props;
        }

        private ComponentSpec(Builder builder) {
            this.type = builder.type;
            this.props = builder.props;
        }

        public static Builder builder() { return new Builder(); }

        public String getType() { return type; }
        public Map<String, Object> getProps() { return props; }

        @Override
        public String toString() {
            return "ComponentSpec(type=" + type + ", props=" + props + ")";
        }

        public static class Builder {
            private String type;
            private Map<String, Object> props;

            public Builder type(String type) { this.type = type; return this; }
            public Builder props(Map<String, Object> props) { this.props = props; return this; }
            public ComponentSpec build() { return new ComponentSpec(this); }
        }
    }
}
