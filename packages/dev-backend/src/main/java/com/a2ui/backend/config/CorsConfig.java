package com.a2ui.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration for A2UI backend.
 *
 * Allows cross-origin requests from configured origins (default: localhost:5173).
 * Enables credentials for session-based authentication.
 */
@Configuration
public class CorsConfig {

    @Value("${a2ui.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Value("${a2ui.cors.allowed-methods:GET,POST,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${a2ui.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${a2ui.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${a2ui.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Set allowed origins
        config.setAllowedOrigins(allowedOrigins);

        // Set allowed HTTP methods
        config.setAllowedMethods(allowedMethods);

        // Set allowed headers
        if (allowedHeaders.size() == 1 && "*".equals(allowedHeaders.get(0))) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(allowedHeaders);
        }

        // Enable credentials (cookies, authorization headers)
        config.setAllowCredentials(allowCredentials);

        // Set max age for preflight cache
        config.setMaxAge(maxAge);

        // Expose headers that frontend can read
        config.setExposedHeaders(List.of(
            "Content-Type",
            "X-Session-Id",
            "X-Request-Id"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/ws/**", config);

        return new CorsFilter(source);
    }
}
