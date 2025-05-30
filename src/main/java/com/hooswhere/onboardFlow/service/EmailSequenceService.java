package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.entity.EmailSequenceEntity;
import com.hooswhere.onboardFlow.entity.EmailStepEntity;
import com.hooswhere.onboardFlow.entity.EmailTemplateEntity;
import com.hooswhere.onboardFlow.models.EmailSequenceConfig;
import com.hooswhere.onboardFlow.models.EmailStepConfig;
import com.hooswhere.onboardFlow.repository.EmailSequenceRepository;
import com.hooswhere.onboardFlow.repository.EmailStepRepository;
import com.hooswhere.onboardFlow.repository.EmailTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailSequenceService {
    private static final Logger logger = LoggerFactory.getLogger(EmailSequenceService.class);
    
    private final EmailSequenceRepository sequenceRepository;
    private final EmailStepRepository stepRepository;
    private final EmailTemplateRepository templateRepository;
    
    public EmailSequenceService(EmailSequenceRepository sequenceRepository, EmailStepRepository stepRepository, EmailTemplateRepository templateRepository) {
        this.sequenceRepository = sequenceRepository;
        this.stepRepository = stepRepository;
        this.templateRepository = templateRepository;
        initializeDefaultSequences();
    }
    
    @Transactional(readOnly = true)
    public Optional<EmailSequenceConfig> getSequenceById(UUID sequenceId) {
        return sequenceRepository.findById(sequenceId)
                .map(this::convertToConfig);
    }
    
    @Transactional(readOnly = true)
    public Optional<EmailSequenceConfig> getSequenceByName(String name) {
        return sequenceRepository.findByName(name)
                .map(this::convertToConfig);
    }
    
    @Transactional(readOnly = true)
    public List<EmailSequenceConfig> getAllActiveSequences() {
        return sequenceRepository.findByIsActiveTrue().stream()
                .map(this::convertToConfig)
                .toList();
    }
    
    /**
     * List all email sequences, active and inactive, ordered by creation date.
     */
    @Transactional(readOnly = true)
    public List<EmailSequenceConfig> listAllSequences() {
        return sequenceRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToConfig)
                .toList();
    }
    
    @Transactional
    public UUID createSequence(String name, String description, int maxDurationDays, List<EmailStepConfig> steps) {
        EmailSequenceEntity sequence = new EmailSequenceEntity();
        sequence.setName(name);
        sequence.setDescription(description);
        sequence.setActive(true);
        sequence.setMaxDurationDays(maxDurationDays);
        
        EmailSequenceEntity savedSequence = sequenceRepository.save(sequence);
        
        for (EmailStepConfig stepConfig : steps) {
            EmailStepEntity step = new EmailStepEntity();
            step.setSequence(savedSequence);
            step.setStepOrder(stepConfig.stepOrder());
            
            // Resolve template by ID first, then by slug if ID is null
            EmailTemplateEntity template;
            if (stepConfig.templateId() != null) {
                template = templateRepository.findById(stepConfig.templateId())
                        .orElseThrow(() -> new IllegalArgumentException("Template not found by ID: " + stepConfig.templateId()));
            } else if (stepConfig.templateSlug() != null) {
                template = templateRepository.findBySlugAndIsActiveTrue(stepConfig.templateSlug())
                        .orElseThrow(() -> new IllegalArgumentException("Template not found by slug: " + stepConfig.templateSlug()));
            } else {
                throw new IllegalArgumentException("Step must have either templateId or templateSlug");
            }
            
            step.setEmailTemplate(template);
            step.setDelayFromStartSeconds((int) stepConfig.delayFromStart().toSeconds());
            step.setSendConditions(stepConfig.sendConditions());
            
            savedSequence.getSteps().add(step);
        }
        
        sequenceRepository.save(savedSequence);
        
        logger.info("Created email sequence: {} with {} steps", name, steps.size());
        return savedSequence.getId();
    }
    
    public boolean validateSequence(EmailSequenceConfig sequence) {
        if (sequence.steps().isEmpty()) {
            logger.warn("Sequence {} has no steps", sequence.name());
            return false;
        }
        
        // Check for duplicate step orders
        long uniqueStepOrders = sequence.steps().stream()
                .mapToInt(EmailStepConfig::stepOrder)
                .distinct()
                .count();
        
        if (uniqueStepOrders != sequence.steps().size()) {
            logger.warn("Sequence {} has duplicate step orders", sequence.name());
            return false;
        }
        
        // Check that step orders are sequential starting from 1
        int expectedSteps = sequence.steps().size();
        boolean hasAllSteps = sequence.steps().stream()
                .mapToInt(EmailStepConfig::stepOrder)
                .allMatch(order -> order >= 1 && order <= expectedSteps);
        
        if (!hasAllSteps) {
            logger.warn("Sequence {} has invalid step ordering", sequence.name());
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public void initializeDefaultSequences() {
        // Only create default sequences if none exist
        if (sequenceRepository.count() == 0) {
            createDefaultSaasSequence();
            createDefaultTrialSequence();
            logger.info("Initialized default email sequences");
        } else {
            logger.info("Default sequences already exist, skipping initialization");
        }
    }
    
    private void createDefaultSaasSequence() {
        List<EmailStepConfig> steps = List.of(
            EmailStepConfig.createWithSlug(null, 1, "welcome", Duration.ZERO, List.of()),
            EmailStepConfig.createWithSlug(null, 2, "getting-started", Duration.ofDays(1), List.of("user_not_active")),
            EmailStepConfig.createWithSlug(null, 3, "feature-highlight", Duration.ofDays(3), List.of("user_not_active", "email_not_opened")),
            EmailStepConfig.createWithSlug(null, 4, "check-in", Duration.ofDays(7), List.of("user_not_converted")),
            EmailStepConfig.createWithSlug(null, 5, "conversion-push", Duration.ofDays(14), List.of("user_not_converted")),
            EmailStepConfig.createWithSlug(null, 6, "final-engagement", Duration.ofDays(30), List.of("user_not_converted"))
        );
        
        createSequence(
            "default-saas-sequence",
            "Default SaaS Onboarding Sequence",
            45,
            steps
        );
    }
    
    private void createDefaultTrialSequence() {
        List<EmailStepConfig> steps = List.of(
            EmailStepConfig.createWithSlug(null, 1, "trial-welcome", Duration.ZERO, List.of()),
            EmailStepConfig.createWithSlug(null, 2, "trial-day-3", Duration.ofDays(3), List.of("user_not_active")),
            EmailStepConfig.createWithSlug(null, 3, "trial-day-7", Duration.ofDays(7), List.of("user_not_converted")),
            EmailStepConfig.createWithSlug(null, 4, "trial-expiring-soon", Duration.ofDays(12), List.of("user_not_converted")),
            EmailStepConfig.createWithSlug(null, 5, "trial-expired", Duration.ofDays(15), List.of("user_not_converted"))
        );
        
        createSequence(
            "trial-sequence",
            "Free Trial Email Sequence",
            21,
            steps
        );
    }
    
    @Transactional
    public void saveSequence(EmailSequenceConfig sequence) {
        if (validateSequence(sequence)) {
            EmailSequenceEntity entity = convertToEntity(sequence);
            sequenceRepository.save(entity);
            logger.info("Saved email sequence: {}", sequence.name());
        } else {
            throw new IllegalArgumentException("Invalid email sequence: " + sequence.name());
        }
    }
    
    @Transactional
    public void activateSequence(UUID sequenceId) {
        EmailSequenceEntity sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceId));
        sequence.setActive(true);
        sequenceRepository.save(sequence);
        logger.info("Activated email sequence: {}", sequence.getName());
    }
    
    @Transactional
    public void deactivateSequence(UUID sequenceId) {
        EmailSequenceEntity sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceId));
        sequence.setActive(false);
        sequenceRepository.save(sequence);
        logger.info("Deactivated email sequence: {}", sequence.getName());
    }
    
    @Transactional
    public void deleteSequence(UUID sequenceId) {
        if (!sequenceRepository.existsById(sequenceId)) {
            throw new IllegalArgumentException("Sequence not found: " + sequenceId);
        }
        sequenceRepository.deleteById(sequenceId);
        logger.info("Deleted email sequence: {}", sequenceId);
    }
    
    private EmailSequenceConfig convertToConfig(EmailSequenceEntity entity) {
        List<EmailStepConfig> steps = entity.getSteps().stream()
                .map(step -> EmailStepConfig.create(
                        entity.getId(),
                        step.getStepOrder(),
                        step.getEmailTemplate().getId(),
                        step.getEmailTemplate().getSlug(),
                        Duration.ofSeconds(step.getDelayFromStartSeconds()),
                        step.getSendConditions() != null ? step.getSendConditions() : List.of()
                ))
                .toList();
        
        return new EmailSequenceConfig(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getMaxDurationDays(),
                steps
        );
    }
    
    private EmailSequenceEntity convertToEntity(EmailSequenceConfig config) {
        EmailSequenceEntity entity = new EmailSequenceEntity();
        entity.setId(config.id());
        entity.setName(config.name());
        entity.setDescription(config.description());
        entity.setActive(config.isActive());
        entity.setMaxDurationDays(config.maxDurationDays());
        
        List<EmailStepEntity> stepEntities = config.steps().stream()
                .map(step -> {
                    EmailStepEntity stepEntity = new EmailStepEntity();
                    stepEntity.setSequence(entity);
                    stepEntity.setStepOrder(step.stepOrder());
                    
                    // Resolve template by ID first, then by slug if ID is null
                    EmailTemplateEntity template;
                    if (step.templateId() != null) {
                        template = templateRepository.findById(step.templateId())
                                .orElseThrow(() -> new IllegalArgumentException("Template not found by ID: " + step.templateId()));
                    } else if (step.templateSlug() != null) {
                        template = templateRepository.findBySlugAndIsActiveTrue(step.templateSlug())
                                .orElseThrow(() -> new IllegalArgumentException("Template not found by slug: " + step.templateSlug()));
                    } else {
                        throw new IllegalArgumentException("Step must have either templateId or templateSlug");
                    }
                    
                    stepEntity.setEmailTemplate(template);
                    stepEntity.setDelayFromStartSeconds((int) step.delayFromStart().toSeconds());
                    stepEntity.setSendConditions(step.sendConditions());
                    return stepEntity;
                })
                .toList();
        
        entity.setSteps(stepEntities);
        return entity;
    }
}