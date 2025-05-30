package com.hooswhere.onboardFlow.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

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
}