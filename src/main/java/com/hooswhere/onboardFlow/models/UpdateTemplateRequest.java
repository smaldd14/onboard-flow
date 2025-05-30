package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing email template")
public record UpdateTemplateRequest(
    @Schema(description = "Template name", example = "Welcome Email v2")
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