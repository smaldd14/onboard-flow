package com.hooswhere.onboardFlow.temporal;

import java.util.List;
import java.util.UUID;

public record ConditionCheckInput(
    UUID customerId,
    String customerEmail,
    List<String> conditions,
    String workflowId,
    int stepNumber
) {
    // Common condition types that can be checked
    public static final String USER_NOT_ACTIVE = "user_not_active";
    public static final String EMAIL_NOT_OPENED = "email_not_opened";
    public static final String USER_NOT_CONVERTED = "user_not_converted";
    public static final String NO_RECENT_LOGIN = "no_recent_login";
    public static final String FEATURE_NOT_USED = "feature_not_used";
}