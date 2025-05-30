package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "Request to start customer onboarding")
public record StartOnboardingRequest(

        @Schema(description = "Customer information", required = true)
        @Valid @NotNull
        CustomerRequest customer,

        @Schema(description = "Email sequence ID to use for onboarding",
                example = "default-saas-sequence")
        @NotBlank
        String sequenceId,

        @Schema(description = "Additional metadata for the onboarding process")
        Map<String, Object> metadata
) {}