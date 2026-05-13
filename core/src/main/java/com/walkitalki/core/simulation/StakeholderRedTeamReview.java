package com.walkitalki.core.simulation;

import java.util.List;

public record StakeholderRedTeamReview(
    MvpGateDecision.Status verdict,
    List<StakeholderRedTeamFinding> findings,
    String independentCheck
) {
    public StakeholderRedTeamReview {
        if (verdict == null) {
            throw new IllegalArgumentException("verdict is required");
        }
        findings = List.copyOf(findings);
        if (findings.isEmpty()) {
            throw new IllegalArgumentException("findings are required");
        }
        if (independentCheck == null || independentCheck.isBlank()) {
            throw new IllegalArgumentException("independentCheck is required");
        }
    }

    public static StakeholderRedTeamReview current() {
        List<StakeholderRedTeamFinding> findings = List.of(
            new StakeholderRedTeamFinding(
                "End users",
                28,
                "The app still cannot prove two people can talk on physical phones without setup friction, latency surprises, or fake-path success.",
                "Physical-device evidence for scan, connect, hold-to-talk, listen, disconnect, and support guidance is missing.",
                "Run two-phone PTT smoke tests across the device matrix, measure time-to-first-talk, and validate understandable permission/pairing/error copy."
            ),
            new StakeholderRedTeamFinding(
                "Product",
                38,
                "Scope can appear larger than MVP because Bluetooth, audio, UI, diagnostics, release, and future transports are all visible at once.",
                "Acceptance evidence is still mostly JVM/debug-shell rather than real MVP outcome evidence.",
                "Keep group, mesh, background, analytics, and codec expansion out of MVP until all eight gates pass on devices."
            ),
            new StakeholderRedTeamFinding(
                "UX/accessibility",
                50,
                "Render models are tested, but there is no production Compose runtime proof for large font, screen reader, orientation, or error recovery flows.",
                "Android UI/screenshot and accessibility evidence is missing.",
                "Bind TalkScreenState to production Compose, add accessibility labels, large-font/orientation checks, and screenshot baselines."
            ),
            new StakeholderRedTeamFinding(
                "Android engineering",
                53,
                "The debug shell proves seams, but lifecycle, process death, foreground service choice, permission callbacks, and device-specific behavior are not validated.",
                "Instrumented lifecycle and permission tests are missing.",
                "Add lifecycle-aware Android adapters, instrumented permission tests, and app-module smoke tests that do not bypass core seams."
            ),
            new StakeholderRedTeamFinding(
                "Bluetooth/transport engineering",
                38,
                "Stream transport and stability plan exist, but real socket lifecycle, pairing failures, weak-signal behavior, and reconnect race conditions are unproven.",
                "Physical-device evidence for the PING/PONG stability matrix is missing.",
                "Connect ClassicBluetoothStreamAdapter to the Android socket boundary and run sustained exchange, disconnect, weak-signal, and pairing retry scenarios."
            ),
            new StakeholderRedTeamFinding(
                "Audio engineering",
                32,
                "Jitter and PTT controller seams pass in JVM, but capture/playback route changes, underruns, latency, and hardware variation are unproven.",
                "Platform capture/playback adapter and latency evidence is missing.",
                "Add platform audio adapters behind AudioInput/AudioOutput after transport stability and benchmark counters, latency, and route changes."
            ),
            new StakeholderRedTeamFinding(
                "QA",
                55,
                "Automation is strong for JVM and preview, but browser pixel checks, Android instrumented checks, and physical device matrix are missing.",
                "Physical-device evidence and browser/Android pixel baseline evidence are missing.",
                "Turn executable plans into CI/manual runs with artifacts for preview pixels, app screenshots, permissions, PING/PONG, and PTT smoke."
            ),
            new StakeholderRedTeamFinding(
                "Privacy/security",
                70,
                "Redaction is executable, but final support export, Android logs, and threat model are not independently reviewed.",
                "Independent privacy review and Android log negative tests are missing.",
                "Version support export schema, run threat review, add Android log negative tests, and verify no identifiers or audio bytes leave diagnostics."
            ),
            new StakeholderRedTeamFinding(
                "Support/operations",
                44,
                "Diagnostics signals exist, but support cannot yet map every blocked physical failure to a user-facing fix.",
                "Support runbook and beta feedback loop are missing.",
                "Create support runbook from release checklist and stability plan signals, then validate it during beta device-matrix runs."
            ),
            new StakeholderRedTeamFinding(
                "Release/business",
                28,
                "There is no signed/beta release path, device matrix signoff, privacy review signoff, or rollback drill.",
                "Release operations evidence is missing.",
                "Complete beta distribution, signed build pipeline, privacy review, store checklist, analytics-free support policy, and rollback plan."
            )
        );
        return new StakeholderRedTeamReview(
            MvpGateDecision.Status.NO_GO,
            findings,
            "Independent check: executable core/preview gates pass locally, but physical-device evidence, Android SDK/instrumented evidence, audio adapter evidence, and release operations evidence are still missing."
        );
    }

    public String renderMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Stakeholder red-team review\n\n");
        markdown.append("Verdict: ").append(verdict).append("\n\n");
        markdown.append(independentCheck).append("\n\n");
        markdown.append("| Stakeholder | Score | Attack | Evidence gap | Route to 100 |\n");
        markdown.append("|---|---:|---|---|---|\n");
        for (StakeholderRedTeamFinding finding : findings) {
            markdown.append("| ").append(finding.stakeholder())
                .append(" | ").append(finding.score())
                .append(" | ").append(finding.attack())
                .append(" | ").append(finding.evidenceGap())
                .append(" | ").append(finding.routeTo100())
                .append(" |\n");
        }
        return markdown.toString();
    }
}
