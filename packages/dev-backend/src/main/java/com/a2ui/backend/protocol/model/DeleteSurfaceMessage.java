package com.a2ui.backend.protocol.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Message to delete a surface and all its components.
 *
 * Example JSON:
 * <pre>
 * {"type":"deleteSurface","surfaceId":"modal1"}
 * </pre>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteSurfaceMessage extends A2uiMessage {

    public static final String TYPE = "deleteSurface";

    /**
     * Creates a delete surface message.
     */
    public static DeleteSurfaceMessage of(String surfaceId) {
        return DeleteSurfaceMessage.builder()
            .type(TYPE)
            .surfaceId(surfaceId)
            .build();
    }
}
