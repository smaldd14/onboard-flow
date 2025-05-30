package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.models.StartOnboardingRequest;

public interface OnboardingStarter {
    void startOnboardingWorkflow(StartOnboardingRequest request);
}
