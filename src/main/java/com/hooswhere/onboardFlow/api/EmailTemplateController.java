package com.hooswhere.onboardFlow.api;

import com.hooswhere.onboardFlow.entity.EmailTemplateEntity;
import com.hooswhere.onboardFlow.models.CreateTemplateRequest;
import com.hooswhere.onboardFlow.models.TemplateListResponse;
import com.hooswhere.onboardFlow.models.TemplatePreviewRequest;
import com.hooswhere.onboardFlow.models.TemplatePreviewResponse;
import com.hooswhere.onboardFlow.models.TemplateResponse;
import com.hooswhere.onboardFlow.models.UpdateTemplateRequest;
import com.hooswhere.onboardFlow.service.EmailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Controller implementation for EmailTemplateApi
 */
@RestController
public class EmailTemplateController implements EmailTemplateApi {
    private static final Logger LOG = LoggerFactory.getLogger(EmailTemplateController.class);
    private final EmailTemplateService templateService;

    public EmailTemplateController(EmailTemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public ResponseEntity<TemplateResponse> createTemplate(CreateTemplateRequest request) {
        try {
            EmailTemplateEntity created = templateService.createTemplate(
                    request.slug(), request.name(), request.subject(), request.htmlBody(), request.textBody()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
        } catch (IllegalArgumentException e) {
            LOG.warn("Template creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<TemplateListResponse> listTemplates() {
        List<TemplateResponse> list = templateService.listAllTemplates().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new TemplateListResponse(list));
    }

    @Override
    public ResponseEntity<TemplateResponse> getTemplate(String id) {
        return templateService.getTemplateEntity(id)
                .map(entity -> ResponseEntity.ok(toResponse(entity)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Override
    public ResponseEntity<TemplateResponse> updateTemplate(String id, UpdateTemplateRequest request) {
        try {
            EmailTemplateEntity updated = templateService.updateTemplate(
                id, request.name(), request.subject(), request.htmlBody(), request.textBody()
            );
            return ResponseEntity.ok(toResponse(updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error updating template {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteTemplate(String id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error deleting template {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<TemplateResponse> activateTemplate(String id) {
        try {
            EmailTemplateEntity entity = templateService.setActive(id, true);
            return ResponseEntity.ok(toResponse(entity));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error activating template {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<TemplateResponse> deactivateTemplate(String id) {
        try {
            EmailTemplateEntity entity = templateService.setActive(id, false);
            return ResponseEntity.ok(toResponse(entity));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error("Error deactivating template {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<TemplatePreviewResponse> previewTemplate(String id, TemplatePreviewRequest request) {
        try {
            var model = templateService.previewTemplate(id, request.variables());
            if (model == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(new TemplatePreviewResponse(
                model.subject(), model.htmlBody(), model.textBody()
            ));
        } catch (Exception e) {
            LOG.error("Error previewing template {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private TemplateResponse toResponse(EmailTemplateEntity e) {
        return new TemplateResponse(
            e.getSlug(), e.getName(), e.getSubject(),
            e.getHtmlBody(), e.getTextBody(),
            e.isActive(), e.getVersion(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}