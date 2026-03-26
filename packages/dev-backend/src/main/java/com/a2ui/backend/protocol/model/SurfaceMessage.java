package com.a2ui.backend.protocol.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Message to create or update a UI surface (container).
 *
 * Example JSON:
 * <pre>
 * {"type":"surface","surfaceId":"main","name":"Chat","metadata":{"theme":"dark"}}
 * </pre>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurfaceMessage extends A2uiMessage {

    public static final String TYPE = "surface";

    /**
     * The display name of the surface.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Optional metadata for the surface configuration.
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Creates a surface message with the default type.
     */
    public static SurfaceMessage of(String surfaceId, String name) {
        return SurfaceMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .name(name)
            .build();
    }

    /**
     * Creates a surface message with metadata.
     */
    public static SurfaceMessage of(String surfaceId, String name, Map<String, Object> metadata) {
        return SurfaceMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .name(name)
            .metadata(metadata)
            .build();
    }

    /**
     * Convenience method to add metadata.
     */
    public SurfaceMessage withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name != null && !name.isEmpty();
    }
}
