package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing email sequence's metadata")
public record UpdateSequenceRequest(
    @Schema(description = "Sequence name", example = "Updated Sequence Name")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Sequence description")
    @Size(max = 2000)
    String description,

    @Schema(description = "Maximum duration of the sequence in days", example = "30")
    @Min(1)
    int maxDurationDays
) {}