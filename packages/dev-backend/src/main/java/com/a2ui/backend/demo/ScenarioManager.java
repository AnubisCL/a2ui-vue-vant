package com.a2ui.backend.demo;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manager for demo scenarios that finds and delegates to appropriate scenario handlers.
 *
 * This component autowires all DemoScenario beans and provides a unified interface
 * to find and execute matching scenarios based on user input.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioManager {

    private final List<DemoScenario> scenarios;

    /**
     * Find a scenario that matches the given user message.
     *
     * @param userMessage The user's input message
     * @return Optional containing the matching scenario, or empty if none matches
     */
    public Optional<DemoScenario> findMatchingScenario(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Optional.empty();
        }

        for (DemoScenario scenario : scenarios) {
            try {
                if (scenario.matches(userMessage)) {
                    log.debug("Scenario '{}' matched for message: {}",
                        scenario.getName(), userMessage);
                    return Optional.of(scenario);
                }
            } catch (Exception e) {
                log.warn("Error checking scenario '{}': {}",
                    scenario.getName(), e.getMessage());
            }
        }

        return Optional.empty();
    }

    /**
     * Check if any scenario matches the given user message.
     *
     * @param userMessage The user's input message
     * @return true if a matching scenario exists
     */
    public boolean hasMatchingScenario(String userMessage) {
        return findMatchingScenario(userMessage).isPresent();
    }

    /**
     * Get all registered scenarios.
     *
     * @return List of all demo scenarios
     */
    public List<DemoScenario> getAllScenarios() {
        return List.copyOf(scenarios);
    }

    /**
     * Get scenario by name.
     *
     * @param name The scenario name
     * @return Optional containing the scenario, or empty if not found
     */
    public Optional<DemoScenario> getScenarioByName(String name) {
        return scenarios.stream()
            .filter(s -> s.getName().equals(name))
            .findFirst();
    }
}
