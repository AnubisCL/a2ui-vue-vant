package com.a2ui.backend.protocol;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON encoding utility for A2UI protocol messages.
 *
 * This class provides methods to encode A2UI messages to JSON strings
 * and JSONL (JSON Lines) format for streaming.
 */
public final class A2uiEncoder {

    private static final ObjectMapper mapper;
    private static final String LINE_SEPARATOR = "\n";

    static {
        mapper = new ObjectMapper();
        // Configure for clean output
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private A2uiEncoder() {
        // Utility class - no instantiation
    }

    /**
     * Returns the configured ObjectMapper instance.
     * Useful for custom serialization needs.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Encodes a single object to JSON string.
     *
     * @param message The object to encode
     * @return JSON string representation
     * @throws A2uiEncodingException if encoding fails
     */
    public static String encode(Object message) {
        if (message == null) {
            throw new A2uiEncodingException("Cannot encode null message");
        }
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new A2uiEncodingException("Failed to encode message: " + e.getMessage(), e);
        }
    }

    /**
     * Encodes a single object to pretty-printed JSON string.
     *
     * @param message The object to encode
     * @return Pretty-printed JSON string representation
     * @throws A2uiEncodingException if encoding fails
     */
    public static String encodePretty(Object message) {
        if (message == null) {
            throw new A2uiEncodingException("Cannot encode null message");
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new A2uiEncodingException("Failed to encode message: " + e.getMessage(), e);
        }
    }

    /**
     * Encodes a list of messages as JSONL (JSON Lines) format.
     * Each message is encoded as a single line, suitable for streaming.
     *
     * @param messages The list of objects to encode
     * @return JSONL string (each JSON object on a new line)
     * @throws A2uiEncodingException if encoding fails
     */
    public static String encodeAsJsonl(List<?> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        return messages.stream()
            .map(A2uiEncoder::encode)
            .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Encodes a list of messages as JSONL with trailing newline.
     *
     * @param messages The list of objects to encode
     * @return JSONL string with trailing newline
     */
    public static String encodeAsJsonlWithNewline(List<?> messages) {
        String jsonl = encodeAsJsonl(messages);
        return jsonl.isEmpty() ? "" : jsonl + LINE_SEPARATOR;
    }

    /**
     * Decodes a JSON string to the specified type.
     *
     * @param json The JSON string to decode
     * @param type The target class
     * @return The decoded object
     * @throws A2uiEncodingException if decoding fails
     */
    public static <T> T decode(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            throw new A2uiEncodingException("Cannot decode empty or null JSON");
        }
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new A2uiEncodingException("Failed to decode JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Exception thrown when encoding/decoding fails.
     */
    public static class A2uiEncodingException extends RuntimeException {
        public A2uiEncodingException(String message) {
            super(message);
        }

        public A2uiEncodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
