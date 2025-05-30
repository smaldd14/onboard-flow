package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Details of an email sequence")
public record SequenceResponse(
    @Schema(description = "UUID of the email sequence")
    UUID id,

    @Schema(description = "Sequence name")
    String name,

    @Schema(description = "Sequence description")
    String description,

    @Schema(description = "Whether the sequence is active or not")
    boolean isActive,

    @Schema(description = "Maximum duration of the sequence in days")
    int maxDurationDays,

    @Schema(description = "Ordered list of steps in this sequence")
    List<StepResponse> steps
) {}