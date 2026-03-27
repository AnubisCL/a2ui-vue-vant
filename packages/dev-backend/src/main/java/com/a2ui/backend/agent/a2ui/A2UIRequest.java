package com.a2ui.backend.agent.a2ui;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class A2UIRequest {
    private String intent;
    private Object data;
    private String displayType;    // "line" | "bar" | "pie" | "form" | "card"
    private String title;
    private Map<String, Object> options;
    private List<Object> chatHistory;  // ChatMessage as List<Object>
}
