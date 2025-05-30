package com.hooswhere.onboardFlow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity for email_steps table.
 */
@Entity
@Table(name = "email_steps")
public class EmailStepEntity {
    /*
    CREATE TABLE IF NOT EXISTS email_steps (
        slug UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        sequence_id UUID REFERENCES email_sequences(slug) ON DELETE CASCADE,
        step_order INTEGER NOT NULL,
        email_template_id UUID REFERENCES email_templates(slug) ON DELETE RESTRICT,
        delay_from_start_seconds INTEGER NOT NULL,
        send_conditions JSONB,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    private EmailSequenceEntity sequence;

    @Column(name = "step_order", nullable = false)
    @Min(value = 1, message = "Step order must be at least 1")
    private int stepOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_template_id", nullable = false)
    private EmailTemplateEntity emailTemplate;

    @Column(name = "delay_from_start_seconds", nullable = false)
    @Min(value = 0, message = "Delay cannot be negative")
    @Max(value = 31536000, message = "Delay cannot exceed 365 days (31,536,000 seconds)")
    private int delayFromStartSeconds;

    @Column(name = "send_conditions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> sendConditions;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    public EmailStepEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EmailSequenceEntity getSequence() {
        return sequence;
    }

    public void setSequence(EmailSequenceEntity sequence) {
        this.sequence = sequence;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public EmailTemplateEntity getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(EmailTemplateEntity emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public int getDelayFromStartSeconds() {
        return delayFromStartSeconds;
    }

    public void setDelayFromStartSeconds(int delayFromStartSeconds) {
        this.delayFromStartSeconds = delayFromStartSeconds;
    }

    public List<String> getSendConditions() {
        return sendConditions;
    }

    public void setSendConditions(List<String> sendConditions) {
        this.sendConditions = sendConditions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailStepEntity that = (EmailStepEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmailStepEntity{" +
                "slug=" + id +
                ", stepOrder=" + stepOrder +
                ", emailTemplate=" + (emailTemplate != null ? emailTemplate.getSlug() : null) +
                ", delayFromStartSeconds=" + delayFromStartSeconds +
                ", sendConditions=" + sendConditions +
                ", createdAt=" + createdAt +
                '}';
    }
}