package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to add a step to an email sequence")
public record CreateStepRequest(
    @Schema(description = "Order of the step within the sequence", example = "1")
    @Min(1)
    int stepOrder,

    @Schema(description = "UUID of the email template to send at this step")
    UUID templateId,

    @Schema(description = "Slug of the email template to send at this step")
    String templateSlug,

    @Schema(description = "Delay from sequence start in minutes before sending this step", example = "1440")
    @Min(0)
    int delayFromStartMinutes,

    @Schema(description = "Conditions that must be met before sending this step")
    List<String> sendConditions
) {}