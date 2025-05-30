package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.models.EmailSequenceConfig;
import com.hooswhere.onboardFlow.models.EmailTemplate;
import com.hooswhere.onboardFlow.models.EmailTemplateContext;
import com.hooswhere.onboardFlow.service.EmailSequenceService;
import com.hooswhere.onboardFlow.service.EmailService;
import com.hooswhere.onboardFlow.service.EmailTemplateService;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ActivityImpl(taskQueues = OnboardingWorkflowImpl.TASK_QUEUE)
public class EmailActivitiesImpl implements EmailActivities {
    private static final Logger logger = LoggerFactory.getLogger(EmailActivitiesImpl.class);
    
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final EmailSequenceService emailSequenceService;
    
    public EmailActivitiesImpl(EmailService emailService, EmailTemplateService emailTemplateService, EmailSequenceService emailSequenceService) {
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.emailSequenceService = emailSequenceService;
    }
    
    @Override
    public void sendEmail(SendEmailInput input) {
        logger.info("Sending email for template: {} to customer: {}", input.emailTemplateId(), input.customerEmail());
        
        try {
            // Build template context
            EmailTemplateContext context = EmailTemplateContext.builder()
                    .customer(input.customerFirstName(), input.customerLastName(), input.customerEmail(), input.companyName())
                    .workflow(input.workflowId(), input.stepNumber())
                    .putAll(input.templateVariables())
                    .build();
            
            // Render the template
            EmailTemplate renderedTemplate = emailTemplateService.renderFullTemplate(input.emailTemplateId(), context);
            
            if (renderedTemplate == null) {
                logger.error("Template not found: {}", input.emailTemplateId());
                throw new RuntimeException("Email template not found: " + input.emailTemplateId());
            }
            
            // Create email tags for tracking
            Map<String, String> emailTags = Map.of(
                    "workflowId", input.workflowId(),
                    "stepNumber", String.valueOf(input.stepNumber()),
                    "templateId", input.emailTemplateId(),
                    "customerId", input.customerId().toString()
            );
            
            // Send the email
            boolean success = emailService.sendEmail(
                    input.customerEmail(),
                    input.customerFirstName() + " " + input.customerLastName(),
                    renderedTemplate.subject(),
                    renderedTemplate.htmlBody(),
                    renderedTemplate.textBody(),
                    emailTags
            );
            
            if (!success) {
                throw new RuntimeException("Failed to send email to: " + input.customerEmail());
            }
            
            logger.info("Successfully sent email {} to {}", input.emailTemplateId(), input.customerEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send email for template: {} to customer: {}", 
                        input.emailTemplateId(), input.customerEmail(), e);
            throw e;
        }
    }
    
    @Override
    public boolean checkEmailConditions(ConditionCheckInput input) {
        logger.debug("Checking conditions for customer: {} - {}", input.customerEmail(), input.conditions());
        
        // For now, implement basic condition checking
        // In a real implementation, this would check user activity, email opens, etc.
        for (String condition : input.conditions()) {
            boolean conditionMet = checkSingleCondition(input, condition);
            if (!conditionMet) {
                logger.debug("Condition not met: {} for customer: {}", condition, input.customerEmail());
                return false;
            }
        }
        
        logger.debug("All conditions met for customer: {}", input.customerEmail());
        return true;
    }
    
    private boolean checkSingleCondition(ConditionCheckInput input, String condition) {
        // Placeholder implementation - in real system, these would check actual data
        switch (condition) {
            case ConditionCheckInput.USER_NOT_ACTIVE:
                // Check if user has logged in recently
                // For now, assume user is not active (condition met)
                return true;
                
            case ConditionCheckInput.EMAIL_NOT_OPENED:
                // Check if previous emails were opened
                // For now, assume email was not opened (condition met)
                return true;
                
            case ConditionCheckInput.USER_NOT_CONVERTED:
                // Check if user has converted (upgraded, purchased, etc.)
                // For now, assume user has not converted (condition met)
                return true;
                
            case ConditionCheckInput.NO_RECENT_LOGIN:
                // Check if user has logged in recently
                // For now, assume no recent login (condition met)
                return true;
                
            case ConditionCheckInput.FEATURE_NOT_USED:
                // Check if user has used key features
                // For now, assume feature not used (condition met)
                return true;
                
            default:
                logger.warn("Unknown condition: {}", condition);
                return true; // Default to condition met for unknown conditions
        }
    }
    
    @Override
    public void logEmailEvent(EmailEventInput input) {
        logger.info("Logging email event: {} for customer: {}, template: {}", 
                   input.eventType(), input.customerId(), input.emailTemplateId());
        
        // In a real implementation, this would store the event in the database
        // For now, just log it
        
        // TODO: Implement actual event logging to email_events table
    }
    
    @Override
    public void updateOnboardingProgress(ProgressUpdateInput input) {
        logger.debug("Updating onboarding progress for workflow: {} - Step: {}, Status: {}", 
                    input.workflowId(), input.currentStep(), input.status());
        
        // In a real implementation, this would update the onboarding_progress table
        // For now, just log it
        
        // TODO: Implement actual progress update to onboarding_progress table
    }
    
    @Override
    public EmailSequenceConfig loadEmailSequence(UUID sequenceId) {
        logger.debug("Loading email sequence: {}", sequenceId);
        
        try {
            return emailSequenceService.getSequenceById(sequenceId)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Failed to load email sequence: {}", sequenceId, e);
            throw e;
        }
    }
}