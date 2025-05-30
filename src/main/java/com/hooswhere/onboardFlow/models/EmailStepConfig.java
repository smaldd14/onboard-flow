package com.hooswhere.onboardFlow.models;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record EmailStepConfig(
    UUID id,
    UUID sequenceId,
    int stepOrder,
    String emailTemplateId,
    Duration delayFromStart,
    List<String> sendConditions
) {
    public static EmailStepConfig create(
            UUID sequenceId,
            int stepOrder,
            String emailTemplateId,
            Duration delayFromStart,
            List<String> sendConditions) {
        return new EmailStepConfig(
                UUID.randomUUID(),
                sequenceId,
                stepOrder,
                emailTemplateId,
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
    
    public long getDelayHours() {
        return delayFromStart.toHours();
    }
    
    public long getDelayMinutes() {
        return delayFromStart.toMinutes();
    }
}