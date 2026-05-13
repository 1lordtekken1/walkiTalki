package com.walkitalki.core.simulation;

public record MvpProgressGate(
    int number,
    String title,
    MvpProgressStatus status,
    String currentEvidence,
    String nextAction,
    boolean majorOpenGate
) {
    public MvpProgressGate {
        if (number < 1) {
            throw new IllegalArgumentException("number must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (currentEvidence == null || currentEvidence.isBlank()) {
            throw new IllegalArgumentException("currentEvidence is required");
        }
        if (nextAction == null || nextAction.isBlank()) {
            throw new IllegalArgumentException("nextAction is required");
        }
    }
}
