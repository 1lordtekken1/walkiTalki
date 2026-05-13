package com.walkitalki.preview;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public final class PreviewVisualBaseline {
    private static final String PREVIEW_SCHEMA = "talk-screen-v1";
    private static final List<String> DEFAULT_SCENARIO_IDS = List.of(
        "permission-blocked",
        "scanning",
        "connecting",
        "ready-to-talk",
        "transmitting",
        "receiving",
        "busy",
        "bluetooth-blocked",
        "disconnected-over-budget"
    );

    private PreviewVisualBaseline() {
    }

    public static Manifest captureDefaultScenarios() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();
        PreviewAutomationAudit.Report audit = PreviewAutomationAudit.audit(html);
        return new Manifest(
            PREVIEW_SCHEMA,
            DEFAULT_SCENARIO_IDS,
            sha256(html),
            html.getBytes(StandardCharsets.UTF_8).length,
            audit.passed()
        );
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for preview visual baselines", exception);
        }
    }

    public record Manifest(
        String previewSchema,
        List<String> scenarioIds,
        String sha256,
        int byteCount,
        boolean auditPassed
    ) {
        public Manifest {
            scenarioIds = List.copyOf(scenarioIds);
            if (previewSchema == null || previewSchema.isBlank()) {
                throw new IllegalArgumentException("previewSchema is required");
            }
            if (scenarioIds.isEmpty()) {
                throw new IllegalArgumentException("scenarioIds are required");
            }
            if (sha256 == null || sha256.length() != 64) {
                throw new IllegalArgumentException("sha256 must be 64 hex characters");
            }
            if (byteCount <= 0) {
                throw new IllegalArgumentException("byteCount must be positive");
            }
        }

        public int scenarioCount() {
            return scenarioIds.size();
        }

        public String renderMarkdown() {
            StringBuilder markdown = new StringBuilder();
            markdown.append("# Talk screen visual baseline\n");
            markdown.append("previewSchema=").append(previewSchema).append('\n');
            markdown.append("scenarioCount=").append(scenarioCount()).append('\n');
            markdown.append("scenarioIds=").append(String.join(",", scenarioIds)).append('\n');
            markdown.append("sha256=").append(sha256).append('\n');
            markdown.append("byteCount=").append(byteCount).append('\n');
            markdown.append("audit=").append(auditPassed ? "PASS" : "FAIL").append('\n');
            markdown.append("diagnostics=metadata-only\n");
            return markdown.toString();
        }
    }
}
