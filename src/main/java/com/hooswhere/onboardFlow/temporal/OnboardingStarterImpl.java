package com.hooswhere.onboardFlow.temporal;

import com.hooswhere.onboardFlow.OnboardingAlreadyStartedException;
import com.hooswhere.onboardFlow.OnboardingStatus;
import com.hooswhere.onboardFlow.entity.CustomerEntity;
import com.hooswhere.onboardFlow.entity.OnboardingProgressEntity;
import com.hooswhere.onboardFlow.models.StartOnboardingRequest;
import com.hooswhere.onboardFlow.repository.OnboardingProgressRepository;
import com.hooswhere.onboardFlow.service.CustomerService;
import com.hooswhere.onboardFlow.service.OnboardingService;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class OnboardingStarterImpl implements OnboardingStarter {
    private static final Logger LOG = LoggerFactory.getLogger(OnboardingStarterImpl.class);
    private final WorkflowClient client;
    private final OnboardingService onboardingService;
    private final CustomerService customerService;
    private final OnboardingProgressRepository onboardingProgressRepository;

    public OnboardingStarterImpl(WorkflowClient client, OnboardingService onboardingService,
                                 CustomerService customerService, OnboardingProgressRepository onboardingProgressRepository) {
        this.client = client;
        this.onboardingService = onboardingService;
        this.customerService = customerService;
        this.onboardingProgressRepository = onboardingProgressRepository;
    }
    @Override
    public void startOnboardingWorkflow(StartOnboardingRequest request) throws OnboardingAlreadyStartedException {
        LOG.info("Starting onboarding workflow with request: {}", request);
        if (onboardingService.hasActiveOnboarding(request.customer().email())) {
            throw new OnboardingAlreadyStartedException("Customer already has an active onboarding process: " + request.customer().email());
        }

        // Create or get customer
        CustomerEntity customer = customerService.createOrUpdateCustomer(request.customer());
        LOG.info("Customer created or updated: {}", customer);

        // Generate unique workflow ID
        String workflowId = "onboarding-" + customer.getId() + "-" + System.currentTimeMillis();
        
        // For now, use a hardcoded sequence ID (will be enhanced in Phase 5)
        UUID sequenceId = UUID.fromString("75addbd2-ce82-4cd9-a656-ae2f0fba3618"); // This will come from email sequence service later
        
        // Create onboarding progress record
        OnboardingProgressEntity progress = createOnboardingProgress(customer, sequenceId, workflowId);
        onboardingProgressRepository.save(progress);
        
        // Create workflow input
        OnboardingWorkflowInput workflowInput = OnboardingWorkflowInput.create(
            request.customer(),
            sequenceId,
            workflowId,
            request.metadata()
        );
        
        // Configure workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue("onboarding-task-queue")
            .build();
        
        // Start the workflow
        OnboardingWorkflow workflow = client.newWorkflowStub(OnboardingWorkflow.class, options);
        
        try {
            // Start workflow asynchronously
            WorkflowClient.start(workflow::executeOnboardingSequence, workflowInput);
            LOG.info("Successfully started onboarding workflow: {} for customer: {}", 
                    workflowId, customer.getEmail());
        } catch (Exception e) {
            LOG.error("Failed to start onboarding workflow for customer: {}", customer.getEmail(), e);
            // Clean up the progress record if workflow start fails
            onboardingProgressRepository.delete(progress);
            throw new RuntimeException("Failed to start onboarding workflow", e);
        }
    }
    
    private OnboardingProgressEntity createOnboardingProgress(CustomerEntity customer, UUID sequenceId, String workflowId) {
        OnboardingProgressEntity progress = new OnboardingProgressEntity();
        progress.setCustomerId(customer.getId());
        progress.setSequenceId(sequenceId);
        progress.setWorkflowId(workflowId);
        progress.setStatus(OnboardingStatus.IN_PROGRESS);
        progress.setCurrentStep(0);
        progress.setStartedAt(Instant.now());
        progress.setLastActivityAt(Instant.now());
        progress.setMetadata(Map.of()); // Empty map
        return progress;
    }
}
