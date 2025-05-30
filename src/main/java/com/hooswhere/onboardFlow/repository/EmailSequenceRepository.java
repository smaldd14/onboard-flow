package com.hooswhere.onboardFlow.repository;

import com.hooswhere.onboardFlow.entity.EmailSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmailSequenceEntity CRUD operations.
 */
public interface EmailSequenceRepository extends JpaRepository<EmailSequenceEntity, UUID> {
    Optional<EmailSequenceEntity> findByName(String name);
    List<EmailSequenceEntity> findByIsActiveTrue();
    
    Optional<EmailSequenceEntity> findByNameAndIsActiveTrue(String name);
    List<EmailSequenceEntity> findByMaxDurationDaysLessThanEqual(int maxDays);
    List<EmailSequenceEntity> findByIsActiveTrueOrderByCreatedAtDesc();
    /**
     * Find all sequences ordered by creation date.
     */
    List<EmailSequenceEntity> findAllByOrderByCreatedAtDesc();
}