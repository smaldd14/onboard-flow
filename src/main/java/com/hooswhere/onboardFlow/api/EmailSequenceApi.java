package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.models.CreateSequenceRequest;
import com.hooswhere.onboardFlow.models.CreateStepRequest;
import com.hooswhere.onboardFlow.models.SequenceListResponse;
import com.hooswhere.onboardFlow.models.SequenceResponse;
import com.hooswhere.onboardFlow.models.StepResponse;
import com.hooswhere.onboardFlow.models.UpdateSequenceRequest;
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
import java.util.List;

/**
 * API interface for managing email sequences
 */
@Tag(name = "Email Sequences", description = "Endpoints for email sequence CRUD and step management operations")
@RequestMapping("/api/v1/sequences")
public interface EmailSequenceApi {

    @Operation(summary = "Create a new email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sequence created successfully",
            content = @Content(schema = @Schema(implementation = SequenceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<SequenceResponse> createSequence(
        @Valid @RequestBody CreateSequenceRequest request
    );

    @Operation(summary = "List all email sequences")
    @ApiResponse(responseCode = "200", description = "List of sequences",
        content = @Content(schema = @Schema(implementation = SequenceListResponse.class)))
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<SequenceListResponse> listSequences();

    @Operation(summary = "Get an email sequence by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence found",
            content = @Content(schema = @Schema(implementation = SequenceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<SequenceResponse> getSequence(
        @PathVariable("id") String id
    );

    @Operation(summary = "Update an existing email sequence's metadata")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence updated successfully",
            content = @Content(schema = @Schema(implementation = SequenceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.PUT, produces = "application/json")
    ResponseEntity<SequenceResponse> updateSequence(
        @PathVariable("id") String id,
        @Valid @RequestBody UpdateSequenceRequest request
    );

    @Operation(summary = "Delete an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sequence deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteSequence(
        @PathVariable("id") String id
    );

    @Operation(summary = "Activate an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence activated",
            content = @Content(schema = @Schema(implementation = SequenceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}/activate", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<SequenceResponse> activateSequence(
        @PathVariable("id") String id
    );

    @Operation(summary = "Deactivate an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence deactivated",
            content = @Content(schema = @Schema(implementation = SequenceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}/deactivate", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<SequenceResponse> deactivateSequence(
        @PathVariable("id") String id
    );

    @Operation(summary = "Validate an email sequence configuration")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence is valid"),
        @ApiResponse(responseCode = "400", description = "Sequence is invalid"),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}/validate", method = RequestMethod.POST)
    ResponseEntity<Void> validateSequence(
        @PathVariable("id") String id
    );

    @Operation(summary = "List steps for a given email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of steps",
            content = @Content(schema = @Schema(implementation = StepResponse.class))),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}/steps", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<List<StepResponse>> listSteps(
        @PathVariable("id") String id
    );

    @Operation(summary = "Add a step to an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Step added successfully",
            content = @Content(schema = @Schema(implementation = StepResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid step data"),
        @ApiResponse(responseCode = "404", description = "Sequence not found")
    })
    @RequestMapping(path = "/{slug}/steps", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<StepResponse> addStep(
        @PathVariable("id") String id,
        @Valid @RequestBody CreateStepRequest request
    );

    @Operation(summary = "Update a step in an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Step updated successfully",
            content = @Content(schema = @Schema(implementation = StepResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid step data"),
        @ApiResponse(responseCode = "404", description = "Sequence or step not found")
    })
    @RequestMapping(path = "/{slug}/steps/{order}", method = RequestMethod.PUT, produces = "application/json")
    ResponseEntity<StepResponse> updateStep(
        @PathVariable("id") String id,
        @PathVariable("order") int stepOrder,
        @Valid @RequestBody CreateStepRequest request
    );

    @Operation(summary = "Delete a step from an email sequence")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Step deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Sequence or step not found")
    })
    @RequestMapping(path = "/{slug}/steps/{order}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteStep(
        @PathVariable("id") String id,
        @PathVariable("order") int stepOrder
    );
}