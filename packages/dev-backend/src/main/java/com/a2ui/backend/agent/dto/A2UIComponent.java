package com.a2ui.backend.agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class A2UIComponent {
    private String type;           // "surface" | "component"
    private String surfaceId;
    private String componentId;
    private String name;
    private ComponentSpec component;
    private String position;       // "append" | "prepend" | "replace"

    @Data
    @Builder
    public static class ComponentSpec {
        private String type;       // "Text" | "Chart" | "Form" | "Card" | etc.
        private Map<String, Object> props;
    }
}
