package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rendered email template content")
public record TemplatePreviewResponse(
    @Schema(description = "Rendered subject line")
    String subject,

    @Schema(description = "Rendered HTML body content")
    String htmlBody,

    @Schema(description = "Rendered text body content")
    String textBody
) {}