package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.OnboardingProgressInfo;
import com.hooswhere.onboardFlow.OnboardingStatus;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@WorkflowImpl(taskQueues = OnboardingWorkflowImpl.TASK_QUEUE)
public class OnboardingWorkflowImpl implements OnboardingWorkflow {
    private static final Logger logger = Workflow.getLogger(OnboardingWorkflowImpl.class);
    public static final String TASK_QUEUE = "onboarding-task-queue";
    
    // Workflow state
    private OnboardingWorkflowInput input;
    private OnboardingStatus status = OnboardingStatus.IN_PROGRESS;
    private int currentStep = 0;
    private boolean paused = false;
    private boolean cancelled = false;
    private boolean userActive = false;
    private boolean userConverted = false;
    private LocalDateTime completedAt;
    private Map<String, Object> workflowMetadata = new HashMap<>();
    
    // Activity stub (will be implemented in Phase 4)
    private final EmailActivities emailActivities = Workflow.newActivityStub(
        EmailActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(
                io.temporal.common.RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofMinutes(1))
                    .build()
            )
            .build()
    );

    @Override
    public void executeOnboardingSequence(OnboardingWorkflowInput input) {
        this.input = input;
        this.workflowMetadata.putAll(input.metadata());
        
        logger.info("Starting onboarding workflow for customer: {} with sequence: {}", 
                   input.customer().email(), input.sequenceId());

        try {
            // Update initial progress
            updateProgress();
            
            // For now, we'll simulate email steps with timers
            // This will be enhanced in Phase 4 with actual email sequence loading
            executeEmailSequence();
            
            // Mark as completed if we reach the end without cancellation
            if (!cancelled && !userConverted) {
                status = OnboardingStatus.COMPLETED;
                completedAt = LocalDateTime.now();
                updateProgress();
                logger.info("Onboarding sequence completed for customer: {}", input.customer().email());
            }
            
        } catch (Exception e) {
            logger.error("Error in onboarding workflow for customer: {}", input.customer().email(), e);
            status = OnboardingStatus.CANCELLED;
            updateProgress();
            throw e;
        }
    }
    
    private void executeEmailSequence() {
        // Load email sequence configuration
        // For now, we'll use a hardcoded sequence name
        // In Phase 5, this will come from the workflow input
        String sequenceName = "default-saas-sequence";
        
        logger.info("Executing dynamic email sequence: {} for customer: {}", sequenceName, input.customer().email());
        
        // In a real implementation, we would load the sequence here
        // For now, simulate steps dynamically based on sequence configuration
        executeStep(1, "welcome", Duration.ZERO, java.util.List.of());
        
        if (shouldContinue()) {
            executeStep(2, "getting-started", Duration.ofDays(1), java.util.List.of("user_not_active"));
        }
        
        if (shouldContinue()) {
            executeStep(3, "feature-highlight", Duration.ofDays(3), java.util.List.of("user_not_active", "email_not_opened"));
        }
        
        if (shouldContinue()) {
            executeStep(4, "check-in", Duration.ofDays(7), java.util.List.of("user_not_converted"));
        }
        
        if (shouldContinue()) {
            executeStep(5, "conversion-push", Duration.ofDays(14), java.util.List.of("user_not_converted"));
        }
        
        if (shouldContinue()) {
            executeStep(6, "final-engagement", Duration.ofDays(30), java.util.List.of("user_not_converted"));
        }
    }
    
    private void executeStep(int stepNumber, String emailTemplateId, Duration delay, java.util.List<String> conditions) {
        if (cancelled || userConverted) {
            return;
        }
        
        // Wait for the specified delay
        if (!delay.isZero()) {
            Workflow.sleep(delay);
        }
        
        // Check if we should continue after the delay
        if (!shouldContinue()) {
            return;
        }
        
        currentStep = stepNumber;
        updateProgress();
        
        logger.info("Executing email step {} ({}) for customer: {}", 
                   stepNumber, emailTemplateId, input.customer().email());
        
        try {
            // Check conditions before sending email
            if (!conditions.isEmpty()) {
                ConditionCheckInput conditionInput = new ConditionCheckInput(
                    java.util.UUID.fromString(input.customer().email()), // Using email as UUID for now
                    input.customer().email(),
                    conditions,
                    input.workflowId(),
                    stepNumber
                );
                
                boolean conditionsMet = emailActivities.checkEmailConditions(conditionInput);
                if (!conditionsMet) {
                    logger.info("Conditions not met for step {}, skipping email", stepNumber);
                    return;
                }
            }
            
            // Create send email input
            SendEmailInput sendInput = SendEmailInput.create(
                java.util.UUID.randomUUID(), // Customer UUID - will be properly set in Phase 5
                input.customer().email(),
                input.customer().firstName(),
                input.customer().lastName(),
                input.customer().companyName(),
                emailTemplateId,
                stepNumber,
                input.workflowId(),
                java.util.Map.of() // Additional template variables
            );
            
            // Send the email
            emailActivities.sendEmail(sendInput);
            
            // Log the email event
            EmailEventInput eventInput = EmailEventInput.sent(
                java.util.UUID.randomUUID(), // Customer UUID
                input.workflowId(),
                emailTemplateId,
                stepNumber
            );
            
            emailActivities.logEmailEvent(eventInput);
            
        } catch (Exception e) {
            logger.error("Failed to send email step {} for customer: {}", 
                        stepNumber, input.customer().email(), e);
            
            // Log the failure event
            try {
                EmailEventInput eventInput = EmailEventInput.failed(
                    java.util.UUID.randomUUID(), // Customer UUID
                    input.workflowId(),
                    emailTemplateId,
                    stepNumber,
                    e.getMessage()
                );
                emailActivities.logEmailEvent(eventInput);
            } catch (Exception logError) {
                logger.error("Failed to log email failure event", logError);
            }
            
            // Continue with the sequence even if one email fails
        }
    }
    
    private boolean shouldContinue() {
        // Handle pause signal
        Workflow.await(() -> !paused || cancelled || userConverted);
        
        // Stop if cancelled or converted
        return !cancelled && !userConverted;
    }
    
    private void updateProgress() {
        try {
            // This will be implemented in Phase 4
            // emailActivities.updateOnboardingProgress(createProgressUpdate());
            
            logger.debug("Progress updated for workflow: {} - Step: {}, Status: {}", 
                        input.workflowId(), currentStep, status);
                        
        } catch (Exception e) {
            logger.error("Failed to update progress for workflow: {}", input.workflowId(), e);
        }
    }

    @Override
    public void pauseOnboarding() {
        logger.info("Pausing onboarding for customer: {}", input.customer().email());
        this.paused = true;
        this.status = OnboardingStatus.PAUSED;
        updateProgress();
    }

    @Override
    public void resumeOnboarding() {
        logger.info("Resuming onboarding for customer: {}", input.customer().email());
        this.paused = false;
        this.status = OnboardingStatus.IN_PROGRESS;
        updateProgress();
    }

    @Override
    public void cancelOnboarding() {
        logger.info("Cancelling onboarding for customer: {}", input.customer().email());
        this.cancelled = true;
        this.status = OnboardingStatus.CANCELLED;
        updateProgress();
    }

    @Override
    public void markUserAsActive() {
        logger.info("User marked as active: {}", input.customer().email());
        this.userActive = true;
        workflowMetadata.put("userActivatedAt", LocalDateTime.now().toString());
    }

    @Override
    public void markUserAsConverted() {
        logger.info("User converted: {}", input.customer().email());
        this.userConverted = true;
        this.status = OnboardingStatus.CONVERTED;
        this.completedAt = LocalDateTime.now();
        workflowMetadata.put("convertedAt", completedAt.toString());
        updateProgress();
    }

    @Override
    public OnboardingProgressInfo getProgress() {
        return new OnboardingProgressInfo(
            null, // ID will be set by the database
            input.customer().email(), // Using email as customer identifier for now
            input.sequenceId().toString(),
            input.workflowId(),
            status,
            currentStep,
            input.startedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            completedAt,
            Map.copyOf(workflowMetadata)
        );
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}