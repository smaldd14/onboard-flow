package com.hooswhere.onboardFlow.repository;

import com.hooswhere.onboardFlow.entity.OnboardingProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgressEntity, UUID>{
    @Query("""
        SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END 
        FROM OnboardingProgressEntity op 
        JOIN CustomerEntity c ON op.customerId = c.id 
        WHERE c.email = :email 
        AND op.status IN ('IN_PROGRESS', 'PAUSED')
        """)
    boolean hasActiveOnboardingByEmail(@Param("email") String email);
}
