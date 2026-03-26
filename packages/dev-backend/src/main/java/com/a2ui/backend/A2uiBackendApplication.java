package com.a2ui.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * A2UI Backend Application Entry Point
 *
 * This Spring Boot application provides:
 * - LLM integration via LangChain4j
 * - A2UI protocol support for rich UI responses
 * - Dual transport: HTTP Streaming (SSE) and WebSocket
 * - Demo business scenarios in chatbox interaction
 *
 * Configuration properties are automatically scanned from
 * the com.a2ui.backend.config package.
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.a2ui.backend.config")
public class A2uiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(A2uiBackendApplication.class, args);
    }
}
