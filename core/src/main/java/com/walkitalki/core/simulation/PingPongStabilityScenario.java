package com.walkitalki.core.simulation;

public record PingPongStabilityScenario(
    String name,
    PingPongStabilityStatus status,
    String acceptanceCriteria,
    String rollbackTrigger,
    String diagnosticsSignal
) {
    public PingPongStabilityScenario {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (acceptanceCriteria == null || acceptanceCriteria.isBlank()) {
            throw new IllegalArgumentException("acceptanceCriteria is required");
        }
        if (rollbackTrigger == null || rollbackTrigger.isBlank()) {
            throw new IllegalArgumentException("rollbackTrigger is required");
        }
        if (diagnosticsSignal == null || diagnosticsSignal.isBlank()) {
            throw new IllegalArgumentException("diagnosticsSignal is required");
        }
    }
}
