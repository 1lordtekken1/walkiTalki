package com.walkitalki.core.simulation;

import java.util.List;

public final class ReadinessScorecard {
    private static final String SUMMARY = "Android debug shell and permission adapter exist, but this is not yet an Android MVP.";

    private static final List<ReadinessScore> MODULES = List.of(
        new ReadinessScore("Protocol framing + stream reader", 78, "Pure JVM codec and stream tests pass.", "Add fuzz/property tests, real Bluetooth stream soak, and backwards-compatible version tests."),
        new ReadinessScore("PTT domain state machine", 76, "Core transitions are deterministic in JVM tests.", "Add Android lifecycle/device interruption adapters and two-device race tests."),
        new ReadinessScore("Permission policy", 78, "Android-version-aware policy plus app permission/environment adapter are modeled and tested.", "Add instrumented API-version grant/deny tests and real device Bluetooth readiness evidence."),
        new ReadinessScore("Diagnostics/privacy redaction", 82, "Redaction tests block raw peer/device/audio payload export.", "Version diagnostics schema, add support runbook, and run privacy threat review."),
        new ReadinessScore("Audio seams + jitter baseline", 58, "Fake audio, jitter buffer, and PTT audio controller pass JVM gates.", "Add platform capture/playback adapters, route handling, underrun counters, latency tests, and codec benchmark."),
        new ReadinessScore("Transport seams + fake/stream transport", 66, "Fake, Java stream, heartbeat, discovery, Classic Bluetooth stream adapter, and executable PING/PONG stability plan exercise frames with redacted diagnostics.", "Connect the stream adapter to a real platform socket boundary and run the physical two-device stability matrix with reconnect evidence."),
        new ReadinessScore("Session controller", 57, "Session composes permissions, transport, audio, UI state, and diagnostics seams.", "Add Android ViewModel/lifecycle adapter and UI intent integration tests."),
        new ReadinessScore("Preview/browser QA surface", 68, "Static preview covers MVP states, stable automation hooks, and deterministic visual baseline drift detection.", "Add browser or Android screenshot automation and pixel baselines."),
        new ReadinessScore("Android app module", 42, "Debug app shell, manifest, local smoke harness, screenshots, permission/environment adapter, and TalkScreenState render-model seam compile against the Android SDK.", "Add Compose runtime surface, lifecycle wiring, instrumented permission tests, and physical-device validation."),
        new ReadinessScore("Bluetooth Classic adapter", 18, "ClassicBluetoothStreamAdapter wraps platform socket streams behind VoiceTransport without raw peer diagnostics.", "Connect a real Android socket boundary and validate PING/PONG on physical devices."),
        new ReadinessScore("Release/store readiness", 24, "Docs, ADRs, no-go list, quality gates, and executable release checklist exist.", "Add signed build pipeline, privacy review, support export validation, beta checklist completion, and device matrix.")
    );

    private static final List<ReadinessScore> STAKEHOLDERS = List.of(
        new ReadinessScore("End users", 28, "Behavior is simulated, previewed, and packaged as a debug shell; no real two-phone audio exists.", "Ship installable two-phone app with permission, pairing, connect, PTT, disconnect, and support flows."),
        new ReadinessScore("Product", 42, "MVP scope, no-go list, click budget, rollback triggers, 100 scenarios, and executable stakeholder red-team routes exist.", "Validate MVP acceptance on real devices and explicitly defer group/background/mesh scope."),
        new ReadinessScore("UX/accessibility", 50, "Framework-free presenter, preview states, and TalkScreenState render-model coverage are executable.", "Add Compose UI, large-font/screen-reader/orientation checks, and screenshot baselines."),
        new ReadinessScore("Android engineering", 53, "Core seams, app shell, manifest, debug harness, permission/environment adapter, and render-model UI seam exist.", "Add Compose runtime integration, lifecycle handling, foreground-service decision, and instrumented tests."),
        new ReadinessScore("Bluetooth/transport engineering", 38, "Protocol, stream reader, heartbeat, fake transport, stream adapter, and executable stability plan exist.", "Add platform socket wrapper and run sustained two-device ping/pong stability gate on physical devices."),
        new ReadinessScore("Audio engineering", 32, "Jitter buffer, fake input/output, and PTT controller exist.", "Add platform capture/playback adapters, route changes, underrun metrics, latency and codec benchmarks."),
        new ReadinessScore("QA", 57, "JVM harness, architecture guard, preview checks, deterministic visual baseline manifest, stakeholder red-team review, and 100 simulations run.", "Add browser screenshots, Android instrumented tests, and physical-device matrix."),
        new ReadinessScore("Privacy/security", 70, "Redaction tests and diagnostics constraints are executable.", "Complete threat model, support export schema, store/privacy review, and Android log negative tests."),
        new ReadinessScore("Support/operations", 48, "Diagnostics signals, user-facing messages, and stakeholder evidence gaps are modeled.", "Add copy/export bundle, runbook mapping signals to fixes, and beta feedback loop."),
        new ReadinessScore("Release/business", 28, "Roadmap, quality gates, and executable release checklist exist.", "Complete beta distribution, signed builds, analytics-free support policy, device matrix, and rollback plan.")
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
