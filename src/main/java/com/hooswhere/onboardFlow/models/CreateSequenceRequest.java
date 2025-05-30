package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request to create a new email sequence")
public record CreateSequenceRequest(
    @Schema(description = "Sequence name", example = "SaaS Trial Sequence")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(description = "Sequence description")
    @Size(max = 2000)
    String description,

    @Schema(description = "Maximum duration of the sequence in days", example = "21")
    @Min(1)
    int maxDurationDays,

    @Schema(description = "Ordered list of steps in this sequence")
    @NotEmpty
    @Valid
    List<CreateStepRequest> steps
) {}