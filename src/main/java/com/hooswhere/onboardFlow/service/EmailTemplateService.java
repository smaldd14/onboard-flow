package com.hooswhere.onboardFlow.service;

import com.hooswhere.onboardFlow.models.EmailTemplate;
import com.hooswhere.onboardFlow.models.EmailTemplateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    private final Map<String, EmailTemplate> templates = new ConcurrentHashMap<>();
    
    public EmailTemplateService() {
        initializeDefaultTemplates();
    }
    
    public Optional<EmailTemplate> getTemplate(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }
    
    public String renderTemplate(String templateId, EmailTemplateContext context) {
        EmailTemplate template = templates.get(templateId);
        if (template == null) {
            logger.warn("Template not found: {}", templateId);
            return null;
        }
        
        return renderVariables(template.subject(), context);
    }
    
    public EmailTemplate renderFullTemplate(String templateId, EmailTemplateContext context) {
        EmailTemplate template = templates.get(templateId);
        if (template == null) {
            logger.warn("Template not found: {}", templateId);
            return null;
        }
        
        String renderedSubject = renderVariables(template.subject(), context);
        String renderedHtmlBody = template.hasHtmlBody() ? renderVariables(template.htmlBody(), context) : null;
        String renderedTextBody = template.hasTextBody() ? renderVariables(template.textBody(), context) : null;
        
        return new EmailTemplate(templateId, renderedSubject, renderedHtmlBody, renderedTextBody);
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
    
    public void addTemplate(EmailTemplate template) {
        templates.put(template.templateId(), template);
        logger.info("Added email template: {}", template.templateId());
    }
    
    private void initializeDefaultTemplates() {
        // Welcome email template
        addTemplate(EmailTemplate.create(
            "welcome",
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
        ));
        
        // Getting started template
        addTemplate(EmailTemplate.create(
            "getting-started",
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
        ));
        
        // Feature highlight template
        addTemplate(EmailTemplate.create(
            "feature-highlight",
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
        ));
        
        // Trial templates
        addTemplate(EmailTemplate.create(
            "trial-welcome",
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
        ));
        
        logger.info("Initialized {} default email templates", templates.size());
    }
}