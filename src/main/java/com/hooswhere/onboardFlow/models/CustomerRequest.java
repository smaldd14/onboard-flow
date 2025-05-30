package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Customer information for onboarding")
public record CustomerRequest(

        @Schema(description = "Customer email address", example = "john.doe@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Customer first name", example = "John")
        @NotBlank @Size(min = 1, max = 100)
        String firstName,

        @Schema(description = "Customer last name", example = "Doe")
        @NotBlank @Size(min = 1, max = 100)
        String lastName,

        @Schema(description = "Company name", example = "Acme Corp")
        @Size(max = 255)
        String companyName,

        @Schema(description = "Customer signup date")
        LocalDateTime signupDate,

        @Schema(description = "Additional customer metadata")
        Map<String, Object> metadata
) {}