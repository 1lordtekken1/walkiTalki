package com.walkitalki.core.simulation;

import java.util.List;

public final class MvpProgressTracker {
    private static final List<MvpProgressGate> GATES = List.of(
        new MvpProgressGate(
            1,
            "Android Gradle module and manifest shell",
            MvpProgressStatus.PARTIAL,
            "Debug app module, manifest, APK packaging, screenshots, smoke-report tasks, and lifecycle hooks compile locally behind app-layer seams.",
            "Bind lifecycle hooks to the real Android Activity/Compose host when an SDK runner is available, without moving platform logic into core/UI state.",
            false
        ),
        new MvpProgressGate(
            2,
            "Compose UI over TalkScreenPresenter",
            MvpProgressStatus.PARTIAL,
            "Framework-free presenter, preview, debug custom View, lifecycle-safe ViewModel intent seam, and AppTalkUiRenderer can render TalkScreenState roadmap states; production Compose runtime surface does not.",
            "Bind the TalkScreenState render model to a thin Compose runtime surface and add Android UI/screenshot checks when SDK runner is available.",
            true
        ),
        new MvpProgressGate(
            3,
            "Browser/screenshot automation for preview states",
            MvpProgressStatus.PARTIAL,
            "Preview HTML tests, local screenshot renderer, CI-friendly preview automation audit, and deterministic visual-baseline manifest exist; browser/Android pixel baselines are not wired.",
            "Add browser/Android pixel baseline capture when a runner is available and keep diagnostics metadata-only.",
            true
        ),
        new MvpProgressGate(
            4,
            "Android Bluetooth permission and environment adapter",
            MvpProgressStatus.PARTIAL,
            "JVM-tested AndroidPermissionEnvironmentAdapter maps manifest names into PermissionPolicy and BluetoothEnvironmentState.",
            "Add instrumented API-version grant/deny validation on Android 11 and Android 12+ devices.",
            false
        ),
        new MvpProgressGate(
            5,
            "Bluetooth Classic wrapper around StreamVoiceTransport",
            MvpProgressStatus.PARTIAL,
            "ClassicBluetoothStreamAdapter wraps platform socket streams behind VoiceTransport with redacted peer diagnostics; physical Bluetooth wrapper evidence does not exist yet.",
            "Connect the adapter to a real platform socket boundary and prove two physical devices exchange PING/PONG.",
            true
        ),
        new MvpProgressGate(
            6,
            "Two-device PING/PONG stability gate",
            MvpProgressStatus.BLOCKED,
            "Executable PING/PONG stability plan defines sustained exchange, disconnect, weak-signal, pairing retry, and diagnostics matrix; no physical-device matrix has run in this environment.",
            "Run the executable PING/PONG stability matrix on physical devices with disconnect/weak-signal evidence before realtime audio.",
            true
        ),
        new MvpProgressGate(
            7,
            "Android audio adapters behind AudioInput and AudioOutput",
            MvpProgressStatus.NOT_STARTED,
            "Audio seams, fake input/output, jitter buffer, and PTT controller pass JVM tests only.",
            "Implement platform capture/playback adapters after transport stability and expose underrun/counter diagnostics only.",
            true
        ),
        new MvpProgressGate(
            8,
            "End-to-end PTT MVP smoke test and release checklist",
            MvpProgressStatus.BLOCKED,
            "Debug smoke flow is local/fake and an executable release checklist blocks missing physical/device/privacy operations evidence; no real two-device audio smoke has passed.",
            "Run scan/connect/hold-to-talk/release/disconnect on physical devices within click budget, satisfy every release checklist item, and keep support export redacted.",
            true
        )
    );

    private MvpProgressTracker() {
    }

    public static List<MvpProgressGate> gates() {
        return GATES;
    }

    public static int openMajorGateCount() {
        return (int) GATES.stream().filter(MvpProgressGate::majorOpenGate).count();
    }

    public static String summary() {
        return "8 MVP gates are tracked; gate 1, gate 2, gate 3, gate 4, and gate 5 have partial local evidence. The remaining work is 6 major implementation/validation gates before MVP.";
    }
}
