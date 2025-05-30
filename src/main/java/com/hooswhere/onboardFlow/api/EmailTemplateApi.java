package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.models.CreateTemplateRequest;
import com.hooswhere.onboardFlow.models.UpdateTemplateRequest;
import com.hooswhere.onboardFlow.models.TemplateListResponse;
import com.hooswhere.onboardFlow.models.TemplatePreviewRequest;
import com.hooswhere.onboardFlow.models.TemplatePreviewResponse;
import com.hooswhere.onboardFlow.models.TemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * API interface for managing email templates
 */
@Tag(name = "Email Templates", description = "Endpoints for email template CRUD and preview operations")
@RequestMapping("/api/v1/templates")
public interface EmailTemplateApi {

    @Operation(summary = "Create a new email template")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Template created successfully",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<TemplateResponse> createTemplate(
        @Valid @RequestBody CreateTemplateRequest request
    );

    @Operation(summary = "List all email templates")
    @ApiResponse(responseCode = "200", description = "List of templates",
        content = @Content(schema = @Schema(implementation = TemplateListResponse.class)))
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<TemplateListResponse> listTemplates();

    @Operation(summary = "Get an email template by slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template found",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<TemplateResponse> getTemplate(
        @PathVariable("id") String id
    );

    @Operation(summary = "Update an existing email template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template updated successfully",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.PUT, produces = "application/json")
    ResponseEntity<TemplateResponse> updateTemplate(
        @PathVariable("id") String id,
        @Valid @RequestBody UpdateTemplateRequest request
    );

    @Operation(summary = "Delete an email template")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Template deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteTemplate(
        @PathVariable("id") String id
    );

    @Operation(summary = "Activate an email template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template activated",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}/activate", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<TemplateResponse> activateTemplate(
        @PathVariable("id") String id
    );

    @Operation(summary = "Deactivate an email template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template deactivated",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}/deactivate", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<TemplateResponse> deactivateTemplate(
        @PathVariable("id") String id
    );

    @Operation(summary = "Preview rendered email template with variables")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preview successful",
            content = @Content(schema = @Schema(implementation = TemplatePreviewResponse.class))),
        @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @RequestMapping(path = "/{slug}/preview", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<TemplatePreviewResponse> previewTemplate(
        @PathVariable("id") String id,
        @Valid @RequestBody TemplatePreviewRequest request
    );
}