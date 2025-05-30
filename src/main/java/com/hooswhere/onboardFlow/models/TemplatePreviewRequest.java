package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "Request to preview a rendered email template")
public record TemplatePreviewRequest(
    @Schema(description = "Variables to substitute in the template")
    @NotNull
    Map<String, Object> variables
) {}