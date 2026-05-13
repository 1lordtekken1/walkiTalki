package com.walkitalki.core.simulation;

import java.util.ArrayList;
import java.util.List;

public final class MvpGateEvaluator {
    private MvpGateEvaluator() {
    }

    public static MvpGateDecision evaluate() {
        List<String> blockers = new ArrayList<>();
        addModuleBlocker(blockers, "Android app module");
        addModuleBlocker(blockers, "Bluetooth Classic adapter");
        addModuleBlocker(blockers, "Audio seams + jitter baseline");
        addModuleBlocker(blockers, "Release/store readiness");
        blockers.add("Two-device PTT smoke test");
        blockers.add("Physical weak-signal and disconnect matrix");

        List<String> evidence = List.of(
            "100 simulation scenarios passing",
            "framework-free architecture guard passing",
            "privacy-safe diagnostics export",
            "two-device ping/pong stability evidence",
            "two-device PTT audio smoke evidence",
            "Android permission and lifecycle instrumented evidence"
        );

        return new MvpGateDecision(
            MvpGateDecision.Status.NO_GO,
            "walkiTalki is not ready for MVP until Android, transport, audio, physical-device, privacy, and release gates all pass.",
            blockers,
            evidence
        );
    }

    private static void addModuleBlocker(List<String> blockers, String moduleName) {
        ReadinessScorecard.modules().stream()
            .filter(score -> score.name().equals(moduleName))
            .findFirst()
            .filter(score -> score.score() < 100)
            .ifPresent(score -> blockers.add(score.name()));
    }
}
