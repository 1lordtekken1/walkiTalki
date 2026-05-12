package com.walkitalki.core.simulation;

public record SimulationScenario(
    String id,
    String stakeholder,
    String category,
    String situation,
    String expectedUx,
    UserJourneyResult result,
    String acceptanceCriteria,
    String rollbackTrigger,
    String diagnosticsSignal
) {
    public SimulationScenario {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (stakeholder == null || stakeholder.isBlank()) {
            throw new IllegalArgumentException("stakeholder is required");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (situation == null || situation.isBlank()) {
            throw new IllegalArgumentException("situation is required");
        }
        if (expectedUx == null || expectedUx.isBlank()) {
            throw new IllegalArgumentException("expectedUx is required");
        }
        if (result == null) {
            throw new IllegalArgumentException("result is required");
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
