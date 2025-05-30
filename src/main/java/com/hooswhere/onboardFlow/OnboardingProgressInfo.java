package com.hooswhere.onboardFlow;

import java.time.LocalDateTime;
import java.util.Map;

public record OnboardingProgressInfo(
        String id,
        String customerId,
        String sequenceId,
        String workflowId,
        OnboardingStatus status,
        Integer currentStep,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Map<String, Object> metadata
) {}
