package com.hooswhere.onboardFlow.models;

import java.util.List;
import java.util.UUID;

public record EmailSequenceConfig(
    UUID id,
    String name,
    String description,
    boolean isActive,
    int maxDurationDays,
    List<EmailStepConfig> steps
) {
    public EmailStepConfig getStepByOrder(int stepOrder) {
        return steps.stream()
            .filter(step -> step.stepOrder() == stepOrder)
            .findFirst()
            .orElse(null);
    }
    
    public EmailStepConfig getNextStep(int currentStep) {
        return steps.stream()
            .filter(step -> step.stepOrder() > currentStep)
            .findFirst()
            .orElse(null);
    }
    
    public int getTotalSteps() {
        return steps.size();
    }
    
    public boolean hasMoreSteps(int currentStep) {
        return getNextStep(currentStep) != null;
    }
}