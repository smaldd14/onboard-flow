package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.OnboardingProgressInfo;
import com.hooswhere.onboardFlow.models.StartOnboardingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Tag(name = "Customer Onboarding", description = "API for managing customer onboarding workflows")
@RequestMapping("/api/onboarding")
public interface OnboardingApi {

    @Operation(
            summary = "Start customer onboarding",
            description = "Initiates a new onboarding workflow for a customer with the specified email sequence"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Onboarding started successfully",
                    content = @Content(schema = @Schema(implementation = OnboardingProgressInfo.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Customer already has active onboarding")
    })
    @RequestMapping(value = "/start",
            produces = "application/json",
            method = RequestMethod.POST)
    ResponseEntity<OnboardingProgressInfo> startOnboarding(
            @Valid @RequestBody StartOnboardingRequest request
    );
}
