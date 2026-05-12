package com.walkitalki.core.simulation;

import com.walkitalki.core.permissions.AndroidPermission;

import java.util.ArrayList;
import java.util.List;

public final class SimulationScenarioCatalog {
    private static final List<Definition> DEFINITIONS = List.of(
        new Definition("permissions-nearby-denied-first-run", "user", "permissions", "First run scan without Nearby devices permission.", "Show a specific Nearby devices grant action and keep the user idle."),
        new Definition("permissions-microphone-denied-listen-only", "user", "permissions", "User can receive but has not granted microphone permission.", "Allow listening, block transmit, and explain that microphone is needed only to talk."),
        new Definition("permissions-legacy-location-missing", "support", "permissions", "Android 11 discovery starts without legacy Location permission.", "Request Location for Bluetooth discovery without blaming Bluetooth."),
        new Definition("permissions-partial-bluetooth-connect", "qa", "permissions", "Nearby scan permission exists but connect permission is missing.", "Keep connection blocked and preserve a clear next permission action."),
        new Definition("permissions-revoked-mid-session", "security", "permissions", "Permission is revoked while the user expects to talk.", "Stop transmit path and keep diagnostics free of identifiers."),
        new Definition("permissions-user-denies-twice", "product", "permissions", "User denies required permission twice.", "Repeat the exact blocked reason and avoid dead-end UI copy."),
        new Definition("permissions-background-return", "user", "permissions", "User returns from settings after granting permission.", "Resume at scan/ready state without hidden retries."),
        new Definition("permissions-mic-granted-nearby-denied", "qa", "permissions", "Microphone exists but Nearby devices is denied.", "Do not imply microphone can bypass peer discovery."),
        new Definition("permissions-nearby-granted-mic-denied-ptt", "qa", "permissions", "Nearby devices is granted but PTT is pressed without microphone.", "Block only transmit and keep listen path understandable."),
        new Definition("permissions-enterprise-policy-block", "support", "permissions", "Enterprise policy blocks a runtime permission.", "Expose coarse permission-block diagnostics and actionable copy."),
        new Definition("pairing-peer-not-paired", "user", "pairing", "Selected peer is not paired yet.", "Show pairing required and ask both users to confirm."),
        new Definition("pairing-user-rejects", "user", "pairing", "User rejects Android pairing confirmation.", "Stay rejected until explicit retry; no auto-reconnect loop."),
        new Definition("pairing-peer-rejects", "support", "pairing", "Remote peer rejects pairing.", "Show retry guidance without exposing remote device name."),
        new Definition("pairing-timeout-pocket", "qa", "pairing", "Pairing prompt times out while phone is pocketed.", "Keep a clear timeout/retry state."),
        new Definition("pairing-wrong-peer-selected", "privacy", "pairing", "User taps the wrong nearby device.", "Avoid raw peer names in diagnostics and allow scanning again."),
        new Definition("pairing-two-peers-same-name", "qa", "pairing", "Two nearby devices have similar display names.", "Use redacted correlation in diagnostics, not raw names."),
        new Definition("pairing-after-bluetooth-toggle", "support", "pairing", "Bluetooth toggles during pairing.", "Move to disconnected/blocking state with reconnect guidance."),
        new Definition("pairing-retry-after-rejection", "product", "pairing", "User retries after a rejection.", "Retry requires an explicit action and stays within click budget."),
        new Definition("pairing-remote-app-not-open", "user", "pairing", "Remote phone is paired but app is not ready.", "Keep connecting/scan guidance visible."),
        new Definition("pairing-stale-bond", "qa", "pairing", "A stale OS bond exists but socket setup fails.", "Disconnect cleanly and keep diagnostics supportable."),
        new Definition("transport-weak-signal-walk-away", "user", "transport", "One user walks away until signal is weak and frames are missed.", "Show unstable/disconnected state instead of pretending audio is live."),
        new Definition("transport-elevator-interference", "qa", "transport", "Users enter an elevator and heartbeat windows are missed.", "Degrade then disconnect based on heartbeat policy."),
        new Definition("transport-pocket-body-block", "support", "transport", "Phone is in a pocket and body blocks the radio path.", "Expose coarse health counters without peer identifiers."),
        new Definition("transport-peer-turns-bluetooth-off", "user", "transport", "Peer turns Bluetooth off during a session.", "Show disconnected and wait for explicit reconnect."),
        new Definition("transport-local-turns-bluetooth-off", "user", "transport", "Local user turns Bluetooth off while connected.", "Block session and route user to Bluetooth settings."),
        new Definition("transport-reconnect-button-after-loss", "product", "transport", "User taps reconnect after link loss.", "Start scanning only after explicit user intent."),
        new Definition("transport-no-auto-reconnect-loop", "engineering", "transport", "Auto reconnect tick fires after socket failure.", "Do not create an unbounded reconnect loop."),
        new Definition("transport-stale-pong", "qa", "transport", "Pong arrives too late to count as stable.", "Keep health degraded/disconnected by policy."),
        new Definition("transport-corrupt-frame", "engineering", "transport", "Corrupt protocol frame arrives from stream.", "Map to recoverable protocol error and avoid payload logging."),
        new Definition("transport-oversized-frame", "security", "transport", "Oversized payload header is received.", "Reject before allocating payload memory."),
        new Definition("transport-split-frame-slow-read", "engineering", "transport", "Frame bytes arrive split across reads.", "Decode only after a complete frame is available."),
        new Definition("transport-concatenated-frames", "engineering", "transport", "Multiple frames arrive in one read.", "Decode frames sequentially without losing boundaries."),
        new Definition("transport-peer-disappears-during-scan", "qa", "transport", "Peer disappears before scan window ends.", "Stop bounded scan and report no peer discovered."),
        new Definition("transport-scan-window-expires", "product", "transport", "No devices are found before scan timeout.", "Show retry guidance and diagnostics elapsed time."),
        new Definition("transport-multiple-nearby-peers", "privacy", "transport", "Several nearby peers are present.", "Do not log raw MAC addresses or names."),
        new Definition("audio-remote-muted-forgotten", "user", "audio", "Remote user muted phone volume and forgot.", "Local can transmit; remote-side UI/diagnostics should make listen/output state obvious later."),
        new Definition("audio-local-muted-forgotten", "user", "audio", "Local phone output is muted when peer talks.", "Receiving state remains visible and should guide the user to volume/output checks."),
        new Definition("audio-mic-blocked-by-os", "support", "audio", "OS microphone permission is blocked.", "PTT asks for microphone and keeps listening available."),
        new Definition("audio-press-before-connected", "qa", "audio", "User holds PTT before connection is ready.", "Ignore transmit and keep state explanatory."),
        new Definition("audio-release-with-no-frame", "qa", "audio", "User taps PTT quickly and releases before a frame exists.", "Return to connected with zero-frame diagnostics."),
        new Definition("audio-remote-frame-before-connection", "security", "audio", "Remote audio arrives before session connection.", "Ignore frame, do not enqueue playback, and avoid received-audio diagnostics."),
        new Definition("audio-payload-redaction", "privacy", "audio", "Audio payload exists in diagnostics path.", "Export payload length and sequence only, never raw bytes."),
        new Definition("audio-jitter-out-of-order", "engineering", "audio", "Remote frames arrive out of order.", "Jitter buffer plays frames in sequence."),
        new Definition("audio-jitter-duplicate", "engineering", "audio", "Duplicate remote frame arrives.", "Drop duplicate and preserve buffer bounds."),
        new Definition("audio-jitter-underrun", "qa", "audio", "Playback asks for audio when buffer is empty.", "Count underrun without crashing."),
        new Definition("audio-busy-no-capture", "privacy", "audio", "Local user presses PTT while remote is talking.", "Enter busy and do not capture microphone audio."),
        new Definition("audio-simultaneous-start", "qa", "audio", "Both users begin PTT almost simultaneously.", "Resolve deterministically to busy."),
        new Definition("audio-long-hold", "product", "audio", "User holds PTT for a long message.", "Continue sending bounded frames and show talking state."),
        new Definition("audio-output-device-change", "user", "audio", "Output changes from speaker to Bluetooth headset.", "Keep receiving state clear and avoid raw device logging."),
        new Definition("audio-noise-burst", "qa", "audio", "Environment noise creates a burst of audio frames.", "Do not log payload; keep byte counts only."),
        new Definition("audio-remote-stops-mid-frame", "engineering", "audio", "Remote stops while a frame is partially received.", "Map stream error and show disconnect or recoverable state."),
        new Definition("audio-local-backgrounded", "product", "audio", "App is backgrounded while user expects audio.", "MVP blocks unsupported background mode and documents no-go state."),
        new Definition("audio-screen-locked", "user", "audio", "Screen locks during receive.", "Future foreground-service gate must preserve listen diagnostics."),
        new Definition("ui-first-run-empty", "user", "ui_ux", "User opens app for the first time with no permissions.", "Primary action explains scan/permission path."),
        new Definition("ui-permission-copy-specific", "ux", "ui_ux", "Permission is blocked.", "Copy names the exact permission and reason."),
        new Definition("ui-ready-state", "ux", "ui_ux", "Connected and stable.", "Show Ready. Hold to talk. and enabled PTT."),
        new Definition("ui-talking-state", "ux", "ui_ux", "Local user is transmitting.", "Show Talking… and release affordance."),
        new Definition("ui-receiving-state", "ux", "ui_ux", "Peer is talking.", "Disable PTT and show listen state."),
        new Definition("ui-busy-state", "ux", "ui_ux", "Both sides talk at once.", "Show peer talking/busy guidance."),
        new Definition("ui-disconnected-state", "ux", "ui_ux", "Socket fails.", "Show Reconnect and diagnostics visibility."),
        new Definition("ui-over-click-budget", "product", "ui_ux", "Happy path exceeds action budget.", "Expose rollback trigger for QA."),
        new Definition("ui-bluetooth-off", "user", "ui_ux", "Bluetooth is off.", "Offer Bluetooth settings action."),
        new Definition("ui-connecting-spinner", "ux", "ui_ux", "Connection is in progress.", "Disable PTT and show wait state."),
        new Definition("ui-scan-timeout", "ux", "ui_ux", "Scan times out.", "Show retry without hiding the reason."),
        new Definition("ui-pairing-rejected", "support", "ui_ux", "Pairing was rejected.", "Show explicit retry guidance."),
        new Definition("ui-diagnostics-copy", "support", "ui_ux", "Support asks user to copy diagnostics.", "Expose coarse diagnostics without identifiers."),
        new Definition("ui-large-font", "accessibility", "ui_ux", "User has large font enabled.", "State labels remain clear and not solely color-coded."),
        new Definition("ui-color-blind", "accessibility", "ui_ux", "User cannot rely on color.", "Text/state labels carry meaning."),
        new Definition("ui-talk-button-disabled", "accessibility", "ui_ux", "PTT is disabled.", "Button disabled state has clear status copy."),
        new Definition("ui-screen-reader-label", "accessibility", "ui_ux", "Screen reader reads primary action.", "Button aria/accessibility label matches action."),
        new Definition("ui-orientation-change", "qa", "ui_ux", "Device orientation changes.", "State survives because presenter is framework-free."),
        new Definition("ui-app-resume", "qa", "ui_ux", "App resumes after being paused.", "Snapshot re-renders current session state."),
        new Definition("ui-no-android-framework-in-core", "engineering", "ui_ux", "UI work starts before Android module.", "Core/preview remain framework-free."),
        new Definition("privacy-peer-mac-redaction", "privacy", "privacy", "Raw MAC address appears in peer id.", "Diagnostics export only peer hash."),
        new Definition("privacy-device-name-redaction", "privacy", "privacy", "Device name contains a person name.", "Diagnostics redact the device name."),
        new Definition("privacy-audio-payload-redaction", "privacy", "privacy", "Audio payload bytes reach diagnostics.", "Diagnostics omit raw payload and show byte count only."),
        new Definition("privacy-preview-no-sensitive-data", "privacy", "privacy", "Preview HTML is shared in a bug report.", "Preview contains only coarse metadata."),
        new Definition("privacy-logs-no-peer-name", "security", "privacy", "Transport timeline is copied to support.", "Timeline reports frame type/counts only."),
        new Definition("privacy-hash-correlation", "support", "privacy", "Support needs to correlate events.", "Use stable peer hash without raw id."),
        new Definition("privacy-crash-after-audio", "security", "privacy", "Crash occurs after receiving audio.", "Support export must not include payload bytes."),
        new Definition("privacy-multiple-users", "privacy", "privacy", "Several peers are nearby.", "Diagnostics must not enumerate raw nearby device names."),
        new Definition("privacy-permission-export", "privacy", "privacy", "Permission state is exported.", "Only coarse grant/block state is exported."),
        new Definition("privacy-qa-screenshot", "privacy", "privacy", "QA screenshot contains preview cards.", "No raw identifiers appear in UI preview."),
        new Definition("reliability-low-battery", "product", "reliability", "Device enters low battery mode.", "Do not claim stable audio without heartbeat evidence."),
        new Definition("reliability-airplane-mode", "user", "reliability", "Airplane mode interrupts radio.", "Show disconnected/Bluetooth blocked path."),
        new Definition("reliability-incoming-call", "user", "reliability", "Incoming call interrupts microphone/audio route.", "Stop or block transmit behind audio seam."),
        new Definition("reliability-notification-sound", "qa", "reliability", "Notification sound plays during receive.", "Keep receive state and avoid payload logging."),
        new Definition("reliability-cpu-pressure", "engineering", "reliability", "CPU pressure delays frame handling.", "Jitter/heartbeat diagnostics reveal degraded health."),
        new Definition("reliability-memory-pressure", "engineering", "reliability", "Memory pressure occurs during buffering.", "Buffers stay bounded."),
        new Definition("reliability-rapid-ptt-toggle", "qa", "reliability", "User rapidly toggles PTT.", "State remains deterministic and counters bounded."),
        new Definition("reliability-double-tap-scan", "qa", "reliability", "User double taps scan.", "Discovery remains bounded and idempotent enough for MVP."),
        new Definition("reliability-peer-reboot", "support", "reliability", "Peer phone reboots mid-session.", "Disconnect and wait for user reconnect."),
        new Definition("reliability-app-process-killed", "engineering", "reliability", "App process is killed.", "Future Android layer must restart cleanly without stale transmit."),
        new Definition("release-two-device-ping-pong", "qa", "release", "Two physical devices run sustained ping/pong.", "Pass physical stability gate before audio transport."),
        new Definition("release-two-device-ptt", "qa", "release", "Two physical devices complete PTT smoke test.", "Meet click budget and audio audibility gate."),
        new Definition("release-diagnostics-export", "support", "release", "User exports support diagnostics.", "Export includes app/coarse counters and no sensitive payload."),
        new Definition("release-rollback-trigger", "product", "release", "A gate fails during MVP validation.", "Rollback/pivot trigger is explicit."),
        new Definition("release-nearby-alternative", "engineering", "release", "Bluetooth Classic fails reliability gate.", "Preserve seams to pivot to Nearby/Wi-Fi Direct."),
        new Definition("release-store-privacy-review", "privacy", "release", "Store/privacy review asks about identifiers.", "Show redaction policy and no raw payload logging."),
        new Definition("release-support-runbook", "support", "release", "Support needs a troubleshooting path.", "Diagnostics and user messages map to runbook actions.")
    );

    private SimulationScenarioCatalog() {
    }

    public static List<SimulationScenario> all() {
        List<SimulationScenario> scenarios = new ArrayList<>(DEFINITIONS.size());
        for (Definition definition : DEFINITIONS) {
            scenarios.add(toScenario(definition));
        }
        return List.copyOf(scenarios);
    }

    private static SimulationScenario toScenario(Definition definition) {
        UserJourneyResult result = simulate(definition);
        return new SimulationScenario(
            definition.id(),
            definition.stakeholder(),
            definition.category(),
            definition.situation(),
            definition.expectedUx(),
            result,
            "Pass when " + definition.expectedUx(),
            "Rollback/pivot if scenario " + definition.id() + " hides the recovery action, exceeds the click budget, or leaks sensitive diagnostics.",
            "simulation:" + definition.category() + ':' + definition.id()
        );
    }

    private static UserJourneyResult simulate(Definition definition) {
        return switch (definition.category()) {
            case "permissions" -> simulatePermissions(definition);
            case "pairing" -> simulatePairing(definition);
            case "transport", "reliability", "release" -> simulateTransport(definition);
            case "audio" -> simulateAudio(definition);
            case "ui_ux" -> simulateUi(definition);
            case "privacy" -> simulatePrivacy(definition);
            default -> connectedJourney().finish();
        };
    }

    private static UserJourneyResult simulatePermissions(Definition definition) {
        if (definition.id().contains("microphone") || definition.id().contains("mic")) {
            return UserJourney.onAndroid(33)
                .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT)
                .scanForPeers()
                .selectPeer("peer-a")
                .connected("peer-a")
                .pressToTalk()
                .finish();
        }
        if (definition.id().contains("legacy")) {
            return UserJourney.onAndroid(30).scanForPeers().finish();
        }
        return UserJourney.onAndroid(33).scanForPeers().finish();
    }

    private static UserJourneyResult simulatePairing(Definition definition) {
        UserJourney journey = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT)
            .scanForPeers()
            .pairingRequired("AA:BB:CC:DD:EE:FF");
        if (definition.id().contains("rejected") || definition.id().contains("rejects")) {
            journey.pairingRejected().autoReconnectTick();
        }
        return journey.finish();
    }

    private static UserJourneyResult simulateTransport(Definition definition) {
        UserJourney journey = connectedJourney();
        if (definition.id().contains("reconnect")) {
            journey.socketFailed("link_loss").autoReconnectTick();
        } else if (definition.id().contains("ping-pong") || definition.id().contains("ptt")) {
            journey.pressToTalk().releaseToListen();
        } else {
            journey.socketFailed(definition.id().contains("weak-signal") ? "weak_signal" : "link_loss");
        }
        return journey.finish();
    }

    private static UserJourneyResult simulateAudio(Definition definition) {
        UserJourney journey = connectedJourney();
        if (definition.id().contains("busy") || definition.id().contains("simultaneous")) {
            journey.pressToTalk().remoteStartsTalking("AA:BB:CC:DD:EE:FF");
        } else if (definition.id().contains("muted") || definition.id().contains("receiv") || definition.id().contains("payload")) {
            journey.remoteStartsTalking("AA:BB:CC:DD:EE:FF").receiveAudio("AA:BB:CC:DD:EE:FF", "Alice Personal Phone", new byte[] {11, 12, 13});
        } else if (definition.id().contains("press-before")) {
            journey = UserJourney.onAndroid(33)
                .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
                .pressToTalk();
        } else {
            journey.pressToTalk().releaseToListen();
        }
        return journey.finish();
    }

    private static UserJourneyResult simulateUi(Definition definition) {
        if (definition.id().contains("permission")) {
            return UserJourney.onAndroid(33).scanForPeers().finish();
        }
        if (definition.id().contains("disconnected") || definition.id().contains("diagnostics")) {
            return connectedJourney().socketFailed("link_loss").finish();
        }
        if (definition.id().contains("talking")) {
            return connectedJourney().pressToTalk().finish();
        }
        if (definition.id().contains("receiving") || definition.id().contains("busy")) {
            return connectedJourney().remoteStartsTalking("peer-a").finish();
        }
        return connectedJourney().finish();
    }

    private static UserJourneyResult simulatePrivacy(Definition definition) {
        return connectedJourney()
            .receiveAudio("AA:BB:CC:DD:EE:FF", "Alice Personal Phone", new byte[] {11, 12, 13})
            .socketFailed("privacy_check")
            .finish();
    }

    private static UserJourney connectedJourney() {
        return UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
            .scanForPeers()
            .selectPeer("AA:BB:CC:DD:EE:FF")
            .connected("AA:BB:CC:DD:EE:FF");
    }

    private record Definition(
        String id,
        String stakeholder,
        String category,
        String situation,
        String expectedUx
    ) {
    }
}
