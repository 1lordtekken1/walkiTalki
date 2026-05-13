package com.walkitalki.core.simulation;

import java.util.List;

public record PingPongStabilityPlan(
    PingPongStabilityStatus status,
    List<PingPongStabilityScenario> scenarios
) {
    public PingPongStabilityPlan {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        scenarios = List.copyOf(scenarios);
        if (scenarios.isEmpty()) {
            throw new IllegalArgumentException("scenarios are required");
        }
        if (status == PingPongStabilityStatus.READY && scenarios.stream().anyMatch(scenario -> scenario.status() == PingPongStabilityStatus.BLOCKED)) {
            throw new IllegalArgumentException("READY plan cannot contain BLOCKED scenarios");
        }
    }

    public static PingPongStabilityPlan current() {
        List<PingPongStabilityScenario> scenarios = List.of(
            new PingPongStabilityScenario(
                "Sustained two-device exchange",
                PingPongStabilityStatus.BLOCKED,
                "Two physical devices exchange PING/PONG long enough to prove stable heartbeat windows before audio work.",
                "Rollback if frame counters stop, heartbeat windows are missed, or a false stable state appears.",
                "transport_ping_pong:sustained_physical_pending"
            ),
            new PingPongStabilityScenario(
                "Disconnect detection",
                PingPongStabilityStatus.BLOCKED,
                "One side disconnects and the session reports disconnected instead of continuing to show stable transport.",
                "Rollback if disconnect does not change health or reconnect loops without user intent.",
                "transport_ping_pong:disconnect_pending"
            ),
            new PingPongStabilityScenario(
                "Weak-signal recovery",
                PingPongStabilityStatus.BLOCKED,
                "Weak-signal or obstructed-device run records degraded health, missed heartbeat windows, and bounded recovery behavior.",
                "Rollback if false stable health hides packet loss or weak-signal recovery drains the battery budget.",
                "transport_ping_pong:weak_signal_pending"
            ),
            new PingPongStabilityScenario(
                "Pairing retry without automatic reconnect",
                PingPongStabilityStatus.BLOCKED,
                "Pairing rejection or timeout can be retried only through explicit user action and never leaks identifiers.",
                "Rollback if automatic reconnect hides pairing failures or diagnostics expose device names.",
                "transport_ping_pong:pairing_retry_pending"
            ),
            new PingPongStabilityScenario(
                "Diagnostics export",
                PingPongStabilityStatus.BLOCKED,
                "Support export includes heartbeat, frame counters, health transitions, and coarse failure reasons only.",
                "Rollback if export includes raw identifiers, addresses, or payload bytes.",
                "transport_ping_pong:diagnostics_pending"
            )
        );
        PingPongStabilityStatus status = scenarios.stream().anyMatch(scenario -> scenario.status() == PingPongStabilityStatus.BLOCKED)
            ? PingPongStabilityStatus.BLOCKED
            : PingPongStabilityStatus.READY;
        return new PingPongStabilityPlan(status, scenarios);
    }

    public String renderMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Two-device PING/PONG stability plan\n\n");
        markdown.append("Status: ").append(status).append("\n\n");
        markdown.append("| Scenario | Status | Acceptance | Rollback | Diagnostics |\n");
        markdown.append("|---|---|---|---|---|\n");
        for (PingPongStabilityScenario scenario : scenarios) {
            markdown.append("| ").append(scenario.name())
                .append(" | ").append(scenario.status())
                .append(" | ").append(scenario.acceptanceCriteria())
                .append(" | ").append(scenario.rollbackTrigger())
                .append(" | ").append(scenario.diagnosticsSignal())
                .append(" |\n");
        }
        return markdown.toString();
    }
}
