package com.walkitalki.core.simulation;

import java.util.List;
import java.util.Objects;

public record MvpGateDecision(
    Status status,
    String summary,
    List<String> blockers,
    List<String> requiredEvidence
) {
    public MvpGateDecision {
        status = Objects.requireNonNull(status, "status");
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("summary is required");
        }
        blockers = List.copyOf(Objects.requireNonNull(blockers, "blockers"));
        requiredEvidence = List.copyOf(Objects.requireNonNull(requiredEvidence, "requiredEvidence"));
        if (status == Status.NO_GO && blockers.isEmpty()) {
            throw new IllegalArgumentException("NO_GO requires blockers");
        }
    }

    public enum Status {
        GO,
        NO_GO
    }
}
