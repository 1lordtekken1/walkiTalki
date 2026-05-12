package com.walkitalki.core.simulation;

import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;

public record UserJourneyResult(
    PttState state,
    PermissionAction lastPermissionAction,
    String userMessage,
    String timeline,
    String diagnostics,
    int userActions
) {
}
