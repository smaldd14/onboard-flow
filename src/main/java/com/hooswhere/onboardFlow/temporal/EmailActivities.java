package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.models.EmailSequenceConfig;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.UUID;

@ActivityInterface
public interface EmailActivities {
    
    @ActivityMethod
    void sendEmail(SendEmailInput input);
    
    @ActivityMethod
    boolean checkEmailConditions(ConditionCheckInput input);
    
    @ActivityMethod
    void logEmailEvent(EmailEventInput input);
    
    @ActivityMethod
    void updateOnboardingProgress(ProgressUpdateInput input);
    
    @ActivityMethod
    EmailSequenceConfig loadEmailSequence(UUID sequenceId);
}