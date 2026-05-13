package com.walkitalki.core.simulation;

import java.util.List;

public record ReleaseChecklist(
    ReleaseChecklistStatus status,
    List<ReleaseChecklistItem> items
) {
    public ReleaseChecklist {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items are required");
        }
        if (status == ReleaseChecklistStatus.GO && items.stream().anyMatch(item -> item.status() == ReleaseChecklistStatus.NO_GO)) {
            throw new IllegalArgumentException("GO checklist cannot contain NO_GO items");
        }
    }

    public static ReleaseChecklist current() {
        List<ReleaseChecklistItem> items = List.of(
            new ReleaseChecklistItem(
                "Android installable artifact",
                ReleaseChecklistStatus.NO_GO,
                "Signed or explicitly debug-scoped APK can be installed and identifies app/build metadata without device identifiers.",
                "Rollback if install, launch, or support metadata require manual hidden steps.",
                "release_artifact:pending"
            ),
            new ReleaseChecklistItem(
                "Permission and Bluetooth readiness",
                ReleaseChecklistStatus.NO_GO,
                "Android 11 legacy discovery and Android 12+ Nearby Devices permission paths are validated on devices.",
                "Rollback if permission checks scatter into UI, transport, or audio layers.",
                "permission_readiness:instrumented_pending"
            ),
            new ReleaseChecklistItem(
                "Two-device PING/PONG stability",
                ReleaseChecklistStatus.NO_GO,
                "Two physical devices sustain PING/PONG with disconnect and weak-signal evidence before realtime audio.",
                "Rollback if false stable state, unbounded reconnect, or pairing dead ends appear.",
                "transport_ping_pong:physical_pending"
            ),
            new ReleaseChecklistItem(
                "Two-device PTT smoke test",
                ReleaseChecklistStatus.NO_GO,
                "Two users complete scan/connect/hold-to-talk/release/disconnect within the click budget on physical devices.",
                "Rollback if happy path exceeds the action budget or succeeds only with fake transport/audio.",
                "ptt_smoke:physical_pending"
            ),
            new ReleaseChecklistItem(
                "Realtime audio adapter evidence",
                ReleaseChecklistStatus.NO_GO,
                "Platform capture/playback adapters expose start/stop, underrun counters, bounded buffers, and no payload logging.",
                "Rollback if UI or domain code calls Android audio APIs directly.",
                "audio_adapter:pending"
            ),
            new ReleaseChecklistItem(
                "Privacy-safe support export",
                ReleaseChecklistStatus.NO_GO,
                "Support export contains app/build, permission state, transport counters, audio counters, and redacted error reasons only.",
                "Rollback if export includes unredacted peer IDs, device names, MAC addresses, or audio bytes.",
                "support_export:redacted_pending"
            ),
            new ReleaseChecklistItem(
                "Release operations checklist",
                ReleaseChecklistStatus.NO_GO,
                "Beta distribution, privacy review, support runbook, rollback plan, and device matrix are complete.",
                "Rollback if support cannot map diagnostics signals to user actions.",
                "release_ops:pending"
            )
        );
        ReleaseChecklistStatus status = items.stream().anyMatch(item -> item.status() == ReleaseChecklistStatus.NO_GO)
            ? ReleaseChecklistStatus.NO_GO
            : ReleaseChecklistStatus.GO;
        return new ReleaseChecklist(status, items);
    }

    public String renderMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# MVP release checklist\n\n");
        markdown.append("Status: ").append(status).append("\n\n");
        markdown.append("| Item | Status | Acceptance | Rollback | Diagnostics |\n");
        markdown.append("|---|---|---|---|---|\n");
        for (ReleaseChecklistItem item : items) {
            markdown.append("| ").append(item.name())
                .append(" | ").append(item.status())
                .append(" | ").append(item.acceptanceCriteria())
                .append(" | ").append(item.rollbackTrigger())
                .append(" | ").append(item.diagnosticsSignal())
                .append(" |\n");
        }
        return markdown.toString();
    }
}
