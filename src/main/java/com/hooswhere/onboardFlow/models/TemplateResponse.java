package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Email template details")
public record TemplateResponse(
    @Schema(description = "Template slug identifier", example = "welcome-email")
    String slug,

    @Schema(description = "Template display name", example = "Welcome Email v2")
    String name,

    @Schema(description = "Email subject line", example = "Welcome to {{companyName}}, {{firstName}}!")
    String subject,

    @Schema(description = "HTML body content of the email")
    String htmlBody,

    @Schema(description = "Text body content of the email")
    String textBody,

    @Schema(description = "Whether the template is active or not")
    boolean isActive,

    @Schema(description = "Version number of the template")
    int version,

    @Schema(description = "Timestamp when the template was created")
    Instant createdAt,

    @Schema(description = "Timestamp when the template was last updated")
    Instant updatedAt
) {}