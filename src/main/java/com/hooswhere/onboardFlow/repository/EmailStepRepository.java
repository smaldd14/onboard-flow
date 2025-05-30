package com.hooswhere.onboardFlow.repository;

import com.hooswhere.onboardFlow.entity.EmailStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for EmailStepEntity CRUD operations.
 */
public interface EmailStepRepository extends JpaRepository<EmailStepEntity, UUID> {
    /**
     * Find all steps for a given sequence, ordered by stepOrder.
     */
    List<EmailStepEntity> findBySequence_IdOrderByStepOrderAsc(UUID sequenceId);
}