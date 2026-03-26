package com.a2ui.backend.protocol.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all A2UI protocol messages.
 *
 * A2UI protocol uses JSONL (JSON Lines) format where each line
 * is a JSON object representing a message to update the UI.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class A2uiMessage {

    /**
     * The message type, determining how the frontend should process it.
     * Subclasses should set their own default type.
     */
    @JsonProperty("type")
    protected String type;

    /**
     * Protocol version for future compatibility.
     * Current version is "1.0".
     */
    @JsonProperty("version")
    protected String version = "1.0";

    /**
     * The surface this message targets.
     * A surface is a UI container (like a chat panel, modal, etc).
     */
    @JsonProperty("surfaceId")
    protected String surfaceId;

    /**
     * Returns true if this message has valid required fields.
     */
    @JsonIgnore
    public boolean isValid() {
        return type != null && !type.isEmpty()
            && surfaceId != null && !surfaceId.isEmpty();
    }
}
