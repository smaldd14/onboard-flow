package com.hooswhere.onboardFlow.temporal;

import java.util.Map;
import java.util.UUID;

public record EmailEventInput(
    UUID customerId,
    String workflowId,
    String eventType,
    String emailTemplateId,
    int stepNumber,
    Map<String, Object> eventData
) {
    // Common email event types
    public static final String EVENT_SENT = "sent";
    public static final String EVENT_DELIVERED = "delivered";
    public static final String EVENT_OPENED = "opened";
    public static final String EVENT_CLICKED = "clicked";
    public static final String EVENT_BOUNCED = "bounced";
    public static final String EVENT_FAILED = "failed";
    public static final String EVENT_UNSUBSCRIBED = "unsubscribed";
    
    public static EmailEventInput sent(UUID customerId, String workflowId, String emailTemplateId, int stepNumber) {
        return new EmailEventInput(customerId, workflowId, EVENT_SENT, emailTemplateId, stepNumber, Map.of());
    }
    
    public static EmailEventInput failed(UUID customerId, String workflowId, String emailTemplateId, int stepNumber, String reason) {
        return new EmailEventInput(customerId, workflowId, EVENT_FAILED, emailTemplateId, stepNumber, 
                                  Map.of("reason", reason));
    }
}