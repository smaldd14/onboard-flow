package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.models.CreateSequenceRequest;
import com.hooswhere.onboardFlow.models.CreateStepRequest;
import com.hooswhere.onboardFlow.models.SequenceListResponse;
import com.hooswhere.onboardFlow.models.SequenceResponse;
import com.hooswhere.onboardFlow.models.StepResponse;
import com.hooswhere.onboardFlow.models.UpdateSequenceRequest;
import com.hooswhere.onboardFlow.models.EmailSequenceConfig;
import com.hooswhere.onboardFlow.models.EmailStepConfig;
import com.hooswhere.onboardFlow.service.EmailSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller implementation for EmailSequenceApi
 */
@RestController
public class EmailSequenceController implements EmailSequenceApi {
    private static final Logger LOG = LoggerFactory.getLogger(EmailSequenceController.class);
    private final EmailSequenceService sequenceService;

    public EmailSequenceController(EmailSequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    @Override
    public ResponseEntity<SequenceResponse> createSequence(CreateSequenceRequest request) {
        try {
            List<EmailStepConfig> steps = request.steps().stream()
                .map(s -> {
                    if (s.templateId() != null) {
                        return EmailStepConfig.create(
                            null,
                            s.stepOrder(),
                            s.templateId(),
                            null,
                            Duration.ofMinutes(s.delayFromStartMinutes()),
                            s.sendConditions()
                        );
                    } else {
                        return EmailStepConfig.createWithSlug(
                            null,
                            s.stepOrder(),
                            s.templateSlug(),
                            Duration.ofMinutes(s.delayFromStartMinutes()),
                            s.sendConditions()
                        );
                    }
                })
                .collect(Collectors.toList());
            UUID sequenceId = sequenceService.createSequence(
                request.name(),
                request.description(),
                request.maxDurationDays(),
                steps
            );
            EmailSequenceConfig config = sequenceService.getSequenceById(sequenceId)
                .orElseThrow();
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(config));
        } catch (IllegalArgumentException e) {
            LOG.warn("Failed to create sequence: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error creating sequence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<SequenceListResponse> listSequences() {
        List<SequenceResponse> list = sequenceService.listAllSequences().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(new SequenceListResponse(list));
    }

    @Override
    public ResponseEntity<SequenceResponse> getSequence(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return sequenceService.getSequenceById(uuid)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<SequenceResponse> updateSequence(String id, UpdateSequenceRequest request) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig existing = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            EmailSequenceConfig updated = new EmailSequenceConfig(
                existing.id(),
                request.name(),
                request.description(),
                existing.isActive(),
                request.maxDurationDays(),
                existing.steps()
            );
            sequenceService.saveSequence(updated);
            return ResponseEntity.ok(toResponse(updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error updating sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteSequence(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            sequenceService.deleteSequence(uuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error deleting sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<SequenceResponse> activateSequence(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            sequenceService.activateSequence(uuid);
            EmailSequenceConfig config = sequenceService.getSequenceById(uuid)
                .orElseThrow();
            return ResponseEntity.ok(toResponse(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error activating sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<SequenceResponse> deactivateSequence(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            sequenceService.deactivateSequence(uuid);
            EmailSequenceConfig config = sequenceService.getSequenceById(uuid)
                .orElseThrow();
            return ResponseEntity.ok(toResponse(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error deactivating sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> validateSequence(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig config = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            boolean valid = sequenceService.validateSequence(config);
            if (valid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            LOG.error("Error validating sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<StepResponse>> listSteps(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig config = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            List<StepResponse> steps = config.steps().stream()
                .map(this::toStepResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(steps);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error listing steps for sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<StepResponse> addStep(String id, CreateStepRequest request) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig existing = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            List<EmailStepConfig> updatedSteps = new ArrayList<>(existing.steps());
            EmailStepConfig newStep;
            if (request.templateId() != null) {
                newStep = EmailStepConfig.create(
                    null,
                    request.stepOrder(),
                    request.templateId(),
                    null,
                    Duration.ofMinutes(request.delayFromStartMinutes()),
                    request.sendConditions()
                );
            } else {
                newStep = EmailStepConfig.createWithSlug(
                    null,
                    request.stepOrder(),
                    request.templateSlug(),
                    Duration.ofMinutes(request.delayFromStartMinutes()),
                    request.sendConditions()
                );
            }
            updatedSteps.add(newStep);
            EmailSequenceConfig updated = new EmailSequenceConfig(
                existing.id(),
                existing.name(),
                existing.description(),
                existing.isActive(),
                existing.maxDurationDays(),
                updatedSteps
            );
            sequenceService.saveSequence(updated);
            return ResponseEntity.status(HttpStatus.CREATED).body(toStepResponse(newStep));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid step data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error adding step to sequence {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<StepResponse> updateStep(String id, int stepOrder, CreateStepRequest request) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig existing = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            EmailStepConfig oldStep = existing.getStepByOrder(stepOrder);
            if (oldStep == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            EmailStepConfig updatedStep;
            if (request.templateId() != null) {
                updatedStep = new EmailStepConfig(
                    oldStep.id(),
                    oldStep.sequenceId(),
                    oldStep.stepOrder(),
                    request.templateId(),
                    null,
                    Duration.ofMinutes(request.delayFromStartMinutes()),
                    request.sendConditions()
                );
            } else {
                updatedStep = new EmailStepConfig(
                    oldStep.id(),
                    oldStep.sequenceId(),
                    oldStep.stepOrder(),
                    null,
                    request.templateSlug(),
                    Duration.ofMinutes(request.delayFromStartMinutes()),
                    request.sendConditions()
                );
            }
            List<EmailStepConfig> updatedSteps = existing.steps().stream()
                .map(s -> s.stepOrder() == stepOrder ? updatedStep : s)
                .collect(Collectors.toList());
            EmailSequenceConfig updatedSeq = new EmailSequenceConfig(
                existing.id(),
                existing.name(),
                existing.description(),
                existing.isActive(),
                existing.maxDurationDays(),
                updatedSteps
            );
            sequenceService.saveSequence(updatedSeq);
            return ResponseEntity.ok(toStepResponse(updatedStep));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error updating step {} for sequence {}", stepOrder, id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteStep(String id, int stepOrder) {
        try {
            UUID uuid = UUID.fromString(id);
            EmailSequenceConfig existing = sequenceService.getSequenceById(uuid)
                .orElseThrow(NoSuchElementException::new);
            if (existing.getStepByOrder(stepOrder) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            List<EmailStepConfig> remaining = existing.steps().stream()
                .filter(s -> s.stepOrder() != stepOrder)
                .collect(Collectors.toList());
            EmailSequenceConfig updatedSeq = new EmailSequenceConfig(
                existing.id(),
                existing.name(),
                existing.description(),
                existing.isActive(),
                existing.maxDurationDays(),
                remaining
            );
            sequenceService.saveSequence(updatedSeq);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error deleting step {} for sequence {}", stepOrder, id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private SequenceResponse toResponse(EmailSequenceConfig config) {
        List<StepResponse> steps = config.steps().stream()
            .map(this::toStepResponse)
            .collect(Collectors.toList());
        return new SequenceResponse(
            config.id(),
            config.name(),
            config.description(),
            config.isActive(),
            config.maxDurationDays(),
            steps
        );
    }

    private StepResponse toStepResponse(EmailStepConfig step) {
        return new StepResponse(
            step.stepOrder(),
            step.templateId(),
            step.templateSlug(),
            (int) step.delayFromStart().toMinutes(),
            step.sendConditions()
        );
    }
}