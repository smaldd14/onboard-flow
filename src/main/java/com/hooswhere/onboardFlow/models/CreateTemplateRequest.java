package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new email template")
public record CreateTemplateRequest(
        @Schema(description = "Unique template identifier (slug)", example = "welcome-email")
        @NotBlank @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9-_]+$", message = "Slug must contain only lowercase letters, numbers, hyphens, and underscores")
        String slug,

        @Schema(description = "Template name", example = "Welcome Email")
        @NotBlank @Size(max = 255)
        String name,

        @Schema(description = "Email subject line", example = "Welcome to {{companyName}}, {{firstName}}!")
        @NotBlank @Size(max = 500)
        String subject,

        @Schema(description = "HTML body of the email")
        String htmlBody,

        @Schema(description = "Text body of the email")
        String textBody
) {}