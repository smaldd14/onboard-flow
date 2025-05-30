package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.OnboardingStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ProgressUpdateInput(
    UUID customerId,
    String workflowId,
    OnboardingStatus status,
    int currentStep,
    LocalDateTime lastActivityAt,
    LocalDateTime completedAt,
    Map<String, Object> metadata
) {
    public static ProgressUpdateInput create(
            UUID customerId,
            String workflowId,
            OnboardingStatus status,
            int currentStep,
            Map<String, Object> metadata) {
        return new ProgressUpdateInput(
                customerId,
                workflowId,
                status,
                currentStep,
                LocalDateTime.now(),
                null,
                metadata
        );
    }
    
    public static ProgressUpdateInput completed(
            UUID customerId,
            String workflowId,
            OnboardingStatus status,
            int currentStep,
            Map<String, Object> metadata) {
        LocalDateTime now = LocalDateTime.now();
        return new ProgressUpdateInput(
                customerId,
                workflowId,
                status,
                currentStep,
                now,
                now,
                metadata
        );
    }
}