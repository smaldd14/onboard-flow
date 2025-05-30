package com.hooswhere.onboardFlow.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "aws.ses")
@Validated
public record AwsSesProps(
        // Some can be empty because we might use default credentials from ec2 instance
        String region,
        String accessKey,
        String secretKey,
        @NotEmpty String fromEmail,
        @NotEmpty String fromName
) {}