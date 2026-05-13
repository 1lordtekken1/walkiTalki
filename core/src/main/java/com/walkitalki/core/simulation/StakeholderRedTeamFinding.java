package com.walkitalki.core.simulation;

public record StakeholderRedTeamFinding(
    String stakeholder,
    int score,
    String attack,
    String evidenceGap,
    String routeTo100
) {
    public StakeholderRedTeamFinding {
        if (stakeholder == null || stakeholder.isBlank()) {
            throw new IllegalArgumentException("stakeholder is required");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be 0..100");
        }
        if (attack == null || attack.isBlank()) {
            throw new IllegalArgumentException("attack is required");
        }
        if (evidenceGap == null || evidenceGap.isBlank()) {
            throw new IllegalArgumentException("evidenceGap is required");
        }
        if (routeTo100 == null || routeTo100.isBlank()) {
            throw new IllegalArgumentException("routeTo100 is required");
        }
    }
}
