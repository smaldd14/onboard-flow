package com.hooswhere.onboardFlow.models;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record EmailStepConfig(
    UUID id,
    UUID sequenceId,
    int stepOrder,
    UUID templateId,
    String templateSlug,
    Duration delayFromStart,
    List<String> sendConditions
) {
    public static EmailStepConfig create(
            UUID sequenceId,
            int stepOrder,
            UUID templateId,
            String templateSlug,
            Duration delayFromStart,
            List<String> sendConditions) {
        return new EmailStepConfig(
                UUID.randomUUID(),
                sequenceId,
                stepOrder,
                templateId,
                templateSlug,
                delayFromStart,
                sendConditions != null ? sendConditions : List.of()
        );
    }
    
    // Convenience method for creating with just slug (UUID will be resolved later)
    public static EmailStepConfig createWithSlug(
            UUID sequenceId,
            int stepOrder,
            String templateSlug,
            Duration delayFromStart,
            List<String> sendConditions) {
        return new EmailStepConfig(
                UUID.randomUUID(),
                sequenceId,
                stepOrder,
                null, // Will be resolved when converting to entity
                templateSlug,
                delayFromStart,
                sendConditions != null ? sendConditions : List.of()
        );
    }
    
    public boolean hasConditions() {
        return sendConditions != null && !sendConditions.isEmpty();
    }
    
    public boolean hasCondition(String condition) {
        return sendConditions != null && sendConditions.contains(condition);
    }
    
    public long delayInHours() {
        return delayFromStart.toHours();
    }
    
    public long delayInMinutes() {
        return delayFromStart.toMinutes();
    }
}