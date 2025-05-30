package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.entity.EmailTemplateEntity;
import com.hooswhere.onboardFlow.models.EmailTemplate;
import com.hooswhere.onboardFlow.models.EmailTemplateContext;
import com.hooswhere.onboardFlow.repository.EmailTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    private final EmailTemplateRepository templateRepository;
    
    public EmailTemplateService(EmailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        initializeDefaultTemplates();
    }
    
    public Optional<EmailTemplate> getTemplate(String templateSlug) {
        return templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .map(this::convertToModel);
    }
    
    public String renderTemplate(String templateSlug, EmailTemplateContext context) {
        EmailTemplateEntity entity = templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .orElse(null);
        if (entity == null) {
            logger.warn("Template not found: {}", templateSlug);
            return null;
        }
        
        return renderVariables(entity.getSubject(), context);
    }
    
    public EmailTemplate renderFullTemplate(String templateSlug, EmailTemplateContext context) {
        EmailTemplateEntity entity = templateRepository.findBySlugAndIsActiveTrue(templateSlug)
                .orElse(null);
        if (entity == null) {
            logger.warn("Template not found: {}", templateSlug);
            return null;
        }
        
        String renderedSubject = renderVariables(entity.getSubject(), context);
        String renderedHtmlBody = entity.getHtmlBody() != null ? renderVariables(entity.getHtmlBody(), context) : null;
        String renderedTextBody = entity.getTextBody() != null ? renderVariables(entity.getTextBody(), context) : null;
        
        return new EmailTemplate(entity.getSlug(), renderedSubject, renderedHtmlBody, renderedTextBody);
    }
    
    private String renderVariables(String content, EmailTemplateContext context) {
        if (content == null) return null;
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = context.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    @Transactional
    public EmailTemplateEntity createTemplate(String slug, String name, String subject, String htmlBody, String textBody) {
        if (templateRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Template with slug already exists: " + slug);
        }
        
        EmailTemplateEntity template = new EmailTemplateEntity(slug, name, subject, htmlBody, textBody);
        EmailTemplateEntity saved = templateRepository.save(template);
        logger.info("Created email template: {}", slug);
        return saved;
    }
    
    private EmailTemplate convertToModel(EmailTemplateEntity entity) {
        return new EmailTemplate(
                entity.getSlug(),
                entity.getSubject(),
                entity.getHtmlBody(),
                entity.getTextBody()
        );
    }
    
    @Transactional
    public void initializeDefaultTemplates() {
        // Only create default templates if none exist
        if (templateRepository.count() == 0) {
            // Welcome email template
            createTemplate(
                "welcome",
                "Welcome Email",
                "Welcome to {{companyName}}, {{firstName}}!",
                """
                <html>
                <body>
                    <h1>Welcome {{firstName}}!</h1>
                    <p>We're excited to have you join {{companyName}}.</p>
                    <p>Your journey starts now. Let's get you set up!</p>
                    <p>Best regards,<br>The {{companyName}} Team</p>
                </body>
                </html>
                """,
                """
                Welcome {{firstName}}!
                
                We're excited to have you join {{companyName}}.
                Your journey starts now. Let's get you set up!
                
                Best regards,
                The {{companyName}} Team
                """
            );
            
            // Getting started template
            createTemplate(
                "getting-started",
                "Getting Started Email",
                "Get started with {{companyName}}",
                """
                <html>
                <body>
                    <h1>Let's get you started, {{firstName}}!</h1>
                    <p>It's been 24 hours since you joined us. Here are the first steps:</p>
                    <ul>
                        <li>Complete your profile setup</li>
                        <li>Explore our key features</li>
                        <li>Connect with our community</li>
                    </ul>
                    <p>Need help? We're here for you!</p>
                </body>
                </html>
                """,
                """
                Let's get you started, {{firstName}}!
                
                It's been 24 hours since you joined us. Here are the first steps:
                - Complete your profile setup
                - Explore our key features  
                - Connect with our community
                
                Need help? We're here for you!
                """
            );
            
            // Feature highlight template
            createTemplate(
                "feature-highlight",
                "Feature Highlight Email",
                "Discover what makes {{companyName}} special",
                """
                <html>
                <body>
                    <h1>{{firstName}}, check out this amazing feature!</h1>
                    <p>We noticed you haven't been active lately. Here's something that might interest you:</p>
                    <p>Our advanced analytics dashboard can help you track your progress and achieve your goals faster.</p>
                    <p>Give it a try today!</p>
                </body>
                </html>
                """,
                """
                {{firstName}}, check out this amazing feature!
                
                We noticed you haven't been active lately. Here's something that might interest you:
                
                Our advanced analytics dashboard can help you track your progress and achieve your goals faster.
                
                Give it a try today!
                """
            );
            
            // Trial welcome template
            createTemplate(
                "trial-welcome",
                "Trial Welcome Email",
                "Your {{companyName}} trial starts now!",
                """
                <html>
                <body>
                    <h1>Welcome to your free trial, {{firstName}}!</h1>
                    <p>You have 14 days to explore everything {{companyName}} has to offer.</p>
                    <p>Make the most of your trial period!</p>
                </body>
                </html>
                """,
                """
                Welcome to your free trial, {{firstName}}!
                
                You have 14 days to explore everything {{companyName}} has to offer.
                Make the most of your trial period!
                """
            );
            
            logger.info("Initialized default email templates");
        } else {
            logger.info("Default templates already exist, skipping initialization");
        }
    }

    /**
     * List all email templates ordered by creation date.
     */
    public List<EmailTemplateEntity> listAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get a template entity by slug.
     */
    public Optional<EmailTemplateEntity> getTemplateEntity(String slug) {
        return templateRepository.findBySlug(slug);
    }

    /**
     * Update an existing template and increment version.
     */
    @Transactional
    public EmailTemplateEntity updateTemplate(String slug, String name, String subject, String htmlBody, String textBody) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        entity.setName(name);
        entity.setSubject(subject);
        entity.setHtmlBody(htmlBody);
        entity.setTextBody(textBody);
        entity.setVersion(entity.getVersion() + 1);
        return templateRepository.save(entity);
    }

    /**
     * Delete a template by slug.
     */
    @Transactional
    public void deleteTemplate(String slug) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        templateRepository.delete(entity);
    }

    /**
     * Activate or deactivate a template.
     */
    @Transactional
    public EmailTemplateEntity setActive(String slug, boolean active) {
        EmailTemplateEntity entity = templateRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Template not found: " + slug));
        entity.setActive(active);
        return templateRepository.save(entity);
    }

    /**
     * Preview rendered template with provided variables.
     */
    public EmailTemplate previewTemplate(String slug, Map<String, Object> variables) {
        return renderFullTemplate(slug, EmailTemplateContext.builder().putAll(variables).build());
    }
}