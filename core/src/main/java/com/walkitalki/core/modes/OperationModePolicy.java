package com.walkitalki.core.modes;

public record OperationModePolicy(
    OperationMode mode,
    long scanWindowMillis,
    long heartbeatIntervalMillis,
    int disconnectAfterMissedHeartbeats,
    int jitterBufferCapacityFrames,
    boolean verboseDiagnostics,
    boolean autoReconnectAllowed
) {
    public OperationModePolicy {
        if (scanWindowMillis <= 0) {
            throw new IllegalArgumentException("scanWindowMillis must be positive");
        }
        if (heartbeatIntervalMillis <= 0) {
            throw new IllegalArgumentException("heartbeatIntervalMillis must be positive");
        }
        if (disconnectAfterMissedHeartbeats < 1) {
            throw new IllegalArgumentException("disconnectAfterMissedHeartbeats must be positive");
        }
        if (jitterBufferCapacityFrames < 1) {
            throw new IllegalArgumentException("jitterBufferCapacityFrames must be positive");
        }
    }

    public static OperationModePolicy forMode(OperationMode mode) {
        return switch (mode) {
            case PERFORMANCE -> new OperationModePolicy(
                mode,
                5_000L,
                750L,
                3,
                4,
                false,
                false
            );
            case BALANCED -> new OperationModePolicy(
                mode,
                10_000L,
                1_500L,
                3,
                3,
                false,
                false
            );
            case POWER_SAVER -> new OperationModePolicy(
                mode,
                20_000L,
                5_000L,
                3,
                2,
                false,
                false
            );
            case DIAGNOSTIC -> new OperationModePolicy(
                mode,
                8_000L,
                1_000L,
                2,
                4,
                true,
                false
            );
        };
    }
}
