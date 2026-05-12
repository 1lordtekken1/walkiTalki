package com.walkitalki.core.simulation;

import java.util.List;

public final class ReadinessScorecard {
    private static final String SUMMARY = "Framework-free core vertical slice exists, but this is not yet an Android MVP.";

    private static final List<ReadinessScore> MODULES = List.of(
        new ReadinessScore("Protocol framing + stream reader", 78, "Pure JVM codec and stream tests pass.", "Add fuzz/property tests, real Bluetooth stream soak, and backwards-compatible version tests."),
        new ReadinessScore("PTT domain state machine", 76, "Core transitions are deterministic in JVM tests.", "Add Android lifecycle/device interruption adapters and two-device race tests."),
        new ReadinessScore("Permission policy", 72, "Android-version-aware policy is modeled and tested.", "Add Android permission adapter and instrumented API-version grant/deny tests."),
        new ReadinessScore("Diagnostics/privacy redaction", 82, "Redaction tests block raw peer/device/audio payload export.", "Version diagnostics schema, add support runbook, and run privacy threat review."),
        new ReadinessScore("Audio seams + jitter baseline", 58, "Fake audio, jitter buffer, and PTT audio controller pass JVM gates.", "Add platform capture/playback adapters, route handling, underrun counters, latency tests, and codec benchmark."),
        new ReadinessScore("Transport seams + fake/stream transport", 55, "Fake and Java stream transports exercise frames and heartbeat.", "Wrap platform socket streams and run two-device ping/pong soak with reconnect evidence."),
        new ReadinessScore("Session controller", 57, "Session composes permissions, transport, audio, UI state, and diagnostics seams.", "Add Android ViewModel/lifecycle adapter and UI intent integration tests."),
        new ReadinessScore("Preview/browser QA surface", 64, "Static preview covers MVP states and stable automation hooks.", "Add browser or Android screenshot automation and visual baselines."),
        new ReadinessScore("Android app module", 0, "No Android app module exists in this repository state.", "Create app module, manifest, DI seams, Compose shell, and Android CI gate."),
        new ReadinessScore("Bluetooth Classic adapter", 5, "Only stream transport seam and ADR assumptions exist.", "Implement platform socket wrapper behind VoiceTransport and validate on physical devices."),
        new ReadinessScore("Release/store readiness", 18, "Docs, ADRs, no-go list, and quality gates exist.", "Add signed build pipeline, privacy review, support export, beta checklist, and device matrix.")
    );

    private static final List<ReadinessScore> STAKEHOLDERS = List.of(
        new ReadinessScore("End users", 22, "Behavior is simulated and previewed; no installable app or real audio exists.", "Ship installable two-phone app with permission, pairing, connect, PTT, disconnect, and support flows."),
        new ReadinessScore("Product", 38, "MVP scope, no-go list, click budget, rollback triggers, and 100 scenarios exist.", "Validate MVP acceptance on real devices and explicitly defer group/background/mesh scope."),
        new ReadinessScore("UX/accessibility", 46, "Framework-free presenter and preview states are executable.", "Add Compose UI, large-font/screen-reader/orientation checks, and screenshot baselines."),
        new ReadinessScore("Android engineering", 42, "Core seams are clean and framework-free.", "Add Android module, adapters, lifecycle handling, foreground-service decision, and instrumented tests."),
        new ReadinessScore("Bluetooth/transport engineering", 34, "Protocol, stream reader, heartbeat, fake transport, and simulation risks exist.", "Add platform socket wrapper and sustained two-device ping/pong stability gate."),
        new ReadinessScore("Audio engineering", 32, "Jitter buffer, fake input/output, and PTT controller exist.", "Add platform capture/playback adapters, route changes, underrun metrics, latency and codec benchmarks."),
        new ReadinessScore("QA", 52, "JVM harness, architecture guard, preview checks, and 100 simulations run.", "Add browser screenshots, Android instrumented tests, and physical-device matrix."),
        new ReadinessScore("Privacy/security", 70, "Redaction tests and diagnostics constraints are executable.", "Complete threat model, support export schema, store/privacy review, and Android log negative tests."),
        new ReadinessScore("Support/operations", 44, "Diagnostics signals and user-facing messages are modeled.", "Add copy/export bundle, runbook mapping signals to fixes, and beta feedback loop."),
        new ReadinessScore("Release/business", 24, "Roadmap and quality gates exist.", "Add beta distribution, signed builds, release checklist, analytics-free support policy, and rollback plan.")
    );

    private ReadinessScorecard() {
    }

    public static List<ReadinessScore> modules() {
        return MODULES;
    }

    public static List<ReadinessScore> stakeholders() {
        return STAKEHOLDERS;
    }

    public static String summary() {
        return SUMMARY;
    }
}
