package com.walkitalki.core.simulation;

public record ReadinessScore(
    String name,
    int score,
    String currentEvidence,
    String pathTo100
) {
    public ReadinessScore {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be in 0..100");
        }
        if (currentEvidence == null || currentEvidence.isBlank()) {
            throw new IllegalArgumentException("currentEvidence is required");
        }
        if (pathTo100 == null || pathTo100.isBlank()) {
            throw new IllegalArgumentException("pathTo100 is required");
        }
    }
}
