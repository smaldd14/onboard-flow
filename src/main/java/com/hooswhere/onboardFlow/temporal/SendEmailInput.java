package com.hooswhere.onboardFlow.temporal;

import java.util.Map;
import java.util.UUID;

public record SendEmailInput(
    UUID customerId,
    String customerEmail,
    String customerFirstName,
    String customerLastName,
    String companyName,
    String emailTemplateId,
    int stepNumber,
    String workflowId,
    Map<String, Object> templateVariables
) {
    public static SendEmailInput create(
            UUID customerId,
            String customerEmail,
            String customerFirstName,
            String customerLastName,
            String companyName,
            String emailTemplateId,
            int stepNumber,
            String workflowId,
            Map<String, Object> templateVariables) {
        return new SendEmailInput(
                customerId,
                customerEmail,
                customerFirstName,
                customerLastName,
                companyName,
                emailTemplateId,
                stepNumber,
                workflowId,
                templateVariables != null ? templateVariables : Map.of()
        );
    }
}