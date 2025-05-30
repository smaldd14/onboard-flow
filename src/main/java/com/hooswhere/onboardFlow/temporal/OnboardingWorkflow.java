package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.OnboardingProgressInfo;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OnboardingWorkflow {
    
    @WorkflowMethod
    void executeOnboardingSequence(OnboardingWorkflowInput input);
    
    @SignalMethod
    void pauseOnboarding();
    
    @SignalMethod 
    void resumeOnboarding();
    
    @SignalMethod
    void cancelOnboarding();
    
    @SignalMethod
    void markUserAsActive();
    
    @SignalMethod
    void markUserAsConverted();
    
    @QueryMethod
    OnboardingProgressInfo getProgress();
    
    @QueryMethod
    boolean isPaused();
    
    @QueryMethod
    boolean isCancelled();
}