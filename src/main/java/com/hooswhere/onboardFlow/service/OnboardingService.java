package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.OnboardingStatus;
import com.hooswhere.onboardFlow.repository.OnboardingProgressRepository;
import com.hooswhere.onboardFlow.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OnboardingService {
    private final OnboardingProgressRepository onboardingProgressRepo;
    private final CustomerRepository customerRepository;

    // Define what statuses are considered "active"
    private static final List<OnboardingStatus> ACTIVE_STATUSES =
            List.of(OnboardingStatus.IN_PROGRESS, OnboardingStatus.PAUSED);

    public OnboardingService(OnboardingProgressRepository onboardingProgressRepo, CustomerRepository customerRepository) {
        this.onboardingProgressRepo = onboardingProgressRepo;
        this.customerRepository = customerRepository;
    }

    public boolean hasActiveOnboarding(String email) {
        return onboardingProgressRepo.hasActiveOnboardingByEmail(email);
    }
}
