package com.a2ui.backend.agent.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class A2UIRequest {
    private final String intent;
    private final Map<String, Object> data;
    private final String displayType;
    private final String title;
    private final Map<String, Object> options;
    private final List<Map<String, Object>> chatHistory;

    @JsonCreator
    public A2UIRequest(
            @JsonProperty("intent") String intent,
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("displayType") String displayType,
            @JsonProperty("title") String title,
            @JsonProperty("options") Map<String, Object> options,
            @JsonProperty("chatHistory") List<Map<String, Object>> chatHistory) {
        this.intent = intent;
        this.data = data;
        this.displayType = displayType;
        this.title = title;
        this.options = options;
        this.chatHistory = chatHistory;
    }

    private A2UIRequest(Builder builder) {
        this.intent = builder.intent;
        this.data = builder.data;
        this.displayType = builder.displayType;
        this.title = builder.title;
        this.options = builder.options;
        this.chatHistory = builder.chatHistory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getIntent() { return intent; }
    public Map<String, Object> getData() { return data; }
    public String getDisplayType() { return displayType; }
    public String getTitle() { return title; }
    public Map<String, Object> getOptions() { return options; }
    public List<Map<String, Object>> getChatHistory() { return chatHistory; }

    @Override
    public String toString() {
        return "A2UIRequest(intent=" + intent + ", data=" + data +
                ", displayType=" + displayType + ", title=" + title +
                ", options=" + options + ", chatHistory=" + chatHistory + ")";
    }

    public static final class Builder {
        private String intent;
        private Map<String, Object> data;
        private String displayType;
        private String title;
        private Map<String, Object> options;
        private List<Map<String, Object>> chatHistory;

        public Builder intent(String intent) { this.intent = intent; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }
        public Builder displayType(String displayType) { this.displayType = displayType; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder options(Map<String, Object> options) { this.options = options; return this; }
        public Builder chatHistory(List<Map<String, Object>> chatHistory) { this.chatHistory = chatHistory; return this; }

        public A2UIRequest build() {
            return new A2UIRequest(this);
        }
    }
}
