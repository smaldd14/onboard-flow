package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.OnboardingAlreadyStartedException;
import com.hooswhere.onboardFlow.OnboardingProgressInfo;
import com.hooswhere.onboardFlow.models.StartOnboardingRequest;
import com.hooswhere.onboardFlow.temporal.OnboardingStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnboardingController implements OnboardingApi {
    private static final Logger LOG = LoggerFactory.getLogger(OnboardingController.class);
    private final OnboardingStarter onboardingStarter;

    public OnboardingController(OnboardingStarter onboardingStarter) {
            this.onboardingStarter = onboardingStarter;
    }
    @Override
    public ResponseEntity<OnboardingProgressInfo> startOnboarding(StartOnboardingRequest request) {
        try {
            onboardingStarter.startOnboardingWorkflow(request);
        } catch (OnboardingAlreadyStartedException e) {
            LOG.warn("Onboarding already started for customer: {}", request.customer().email(), e);
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            LOG.error("Error starting onboarding workflow for customer: {}", request.customer().email(), e);
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok().build();
    }
}
