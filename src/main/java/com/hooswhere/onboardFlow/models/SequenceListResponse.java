package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "List of email sequences")
public record SequenceListResponse(
    @Schema(description = "Collection of email sequences")
    List<SequenceResponse> sequences
) {}