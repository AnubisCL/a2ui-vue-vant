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
 * Message to update the data model for a surface.
 *
 * The data model provides reactive state that components can bind to.
 * When the data model changes, components referencing the data will update.
 *
 * Example JSON:
 * <pre>
 * {"type":"dataModel","surfaceId":"main","data":{"userName":"John","balance":100}}
 * </pre>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataModelMessage extends A2uiMessage {

    public static final String TYPE = "dataModel";

    /**
     * The data model to set/update.
     * This will be merged with existing data (deep merge for nested objects).
     */
    @JsonProperty("data")
    private Map<String, Object> data = new HashMap<>();

    /**
     * Merge strategy: "merge" (default) or "replace".
     * "merge" performs a deep merge with existing data.
     * "replace" completely replaces the data model.
     */
    @JsonProperty("mergeStrategy")
    private String mergeStrategy = "merge";

    /**
     * Creates a data model message with default merge strategy.
     */
    public static DataModelMessage of(String surfaceId, Map<String, Object> data) {
        return DataModelMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .data(data != null ? data : new HashMap<>())
            .mergeStrategy("merge")
            .build();
    }

    /**
     * Creates a data model message with replace strategy.
     */
    public static DataModelMessage replace(String surfaceId, Map<String, Object> data) {
        return DataModelMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .data(data != null ? data : new HashMap<>())
            .mergeStrategy("replace")
            .build();
    }

    /**
     * Creates an empty data model message.
     */
    public static DataModelMessage empty(String surfaceId) {
        return DataModelMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .build();
    }

    /**
     * Convenience method to add a data entry.
     */
    public DataModelMessage withData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && data != null;
    }
}
