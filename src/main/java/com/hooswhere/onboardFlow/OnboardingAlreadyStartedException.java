package com.hooswhere.onboardFlow;

public class OnboardingAlreadyStartedException extends RuntimeException {
    public OnboardingAlreadyStartedException(String message) {
        super(message);
    }

    public OnboardingAlreadyStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}
