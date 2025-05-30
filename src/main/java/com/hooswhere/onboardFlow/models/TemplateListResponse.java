package com.hooswhere.onboardFlow.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "List of email templates")
public record TemplateListResponse(
    @Schema(description = "Collection of email templates")
    List<TemplateResponse> templates
) {}