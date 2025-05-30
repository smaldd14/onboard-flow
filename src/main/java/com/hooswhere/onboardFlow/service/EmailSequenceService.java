package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.models.EmailSequenceConfig;
import com.hooswhere.onboardFlow.models.EmailStepConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailSequenceService {
    private static final Logger logger = LoggerFactory.getLogger(EmailSequenceService.class);
    
    // For now, we'll store sequences in memory
    // In Phase 5, this will be backed by database entities
    private final Map<UUID, EmailSequenceConfig> sequences = new ConcurrentHashMap<>();
    private final Map<String, UUID> sequencesByName = new ConcurrentHashMap<>();
    
    public EmailSequenceService() {
        initializeDefaultSequences();
    }
    
    public Optional<EmailSequenceConfig> getSequenceById(UUID sequenceId) {
        return Optional.ofNullable(sequences.get(sequenceId));
    }
    
    public Optional<EmailSequenceConfig> getSequenceByName(String name) {
        UUID sequenceId = sequencesByName.get(name);
        return sequenceId != null ? getSequenceById(sequenceId) : Optional.empty();
    }
    
    public List<EmailSequenceConfig> getAllActiveSequences() {
        return sequences.values().stream()
                .filter(EmailSequenceConfig::isActive)
                .toList();
    }
    
    public UUID createSequence(String name, String description, int maxDurationDays, List<EmailStepConfig> steps) {
        UUID sequenceId = UUID.randomUUID();
        EmailSequenceConfig sequence = new EmailSequenceConfig(
                sequenceId,
                name,
                description,
                true,
                maxDurationDays,
                steps
        );
        
        sequences.put(sequenceId, sequence);
        sequencesByName.put(name, sequenceId);
        
        logger.info("Created email sequence: {} with {} steps", name, steps.size());
        return sequenceId;
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
    
    private void initializeDefaultSequences() {
        // Create a default SaaS onboarding sequence
        createDefaultSaasSequence();
        
        // Create a simple trial sequence
        createDefaultTrialSequence();
        
        logger.info("Initialized {} default email sequences", sequences.size());
    }
    
    private void createDefaultSaasSequence() {
        List<EmailStepConfig> steps = List.of(
            EmailStepConfig.create(null, 1, "welcome", Duration.ZERO, List.of()),
            EmailStepConfig.create(null, 2, "getting-started", Duration.ofDays(1), List.of("user_not_active")),
            EmailStepConfig.create(null, 3, "feature-highlight", Duration.ofDays(3), List.of("user_not_active", "email_not_opened")),
            EmailStepConfig.create(null, 4, "check-in", Duration.ofDays(7), List.of("user_not_converted")),
            EmailStepConfig.create(null, 5, "conversion-push", Duration.ofDays(14), List.of("user_not_converted")),
            EmailStepConfig.create(null, 6, "final-engagement", Duration.ofDays(30), List.of("user_not_converted"))
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
            EmailStepConfig.create(null, 1, "trial-welcome", Duration.ZERO, List.of()),
            EmailStepConfig.create(null, 2, "trial-day-3", Duration.ofDays(3), List.of("user_not_active")),
            EmailStepConfig.create(null, 3, "trial-day-7", Duration.ofDays(7), List.of("user_not_converted")),
            EmailStepConfig.create(null, 4, "trial-expiring-soon", Duration.ofDays(12), List.of("user_not_converted")),
            EmailStepConfig.create(null, 5, "trial-expired", Duration.ofDays(15), List.of("user_not_converted"))
        );
        
        createSequence(
            "trial-sequence",
            "Free Trial Email Sequence",
            21,
            steps
        );
    }
    
    public void addSequence(EmailSequenceConfig sequence) {
        if (validateSequence(sequence)) {
            sequences.put(sequence.id(), sequence);
            sequencesByName.put(sequence.name(), sequence.id());
            logger.info("Added email sequence: {}", sequence.name());
        } else {
            throw new IllegalArgumentException("Invalid email sequence: " + sequence.name());
        }
    }
}