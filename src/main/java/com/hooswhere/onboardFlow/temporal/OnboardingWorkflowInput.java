package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.models.CustomerRequest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OnboardingWorkflowInput(
    CustomerRequest customer,
    UUID sequenceId,
    String workflowId,
    Instant startedAt,
    Map<String, Object> metadata
) {
    public static OnboardingWorkflowInput create(
            CustomerRequest customer,
            UUID sequenceId,
            String workflowId,
            Map<String, Object> metadata) {
        return new OnboardingWorkflowInput(
                customer,
                sequenceId,
                workflowId,
                Instant.now(),
                metadata != null ? metadata : Map.of()
        );
    }
}