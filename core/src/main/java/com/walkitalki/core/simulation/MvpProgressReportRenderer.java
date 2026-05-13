package com.walkitalki.core.simulation;

public final class MvpProgressReportRenderer {
    private MvpProgressReportRenderer() {
    }

    public static String renderMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# MVP progress tracker\n\n");
        markdown.append(MvpProgressTracker.summary()).append("\n\n");
        markdown.append("Open major gates: ").append(MvpProgressTracker.openMajorGateCount()).append("\n\n");
        markdown.append("| # | Gate | Status | Current evidence | Next action |\n");
        markdown.append("|---:|---|---|---|---|\n");
        for (MvpProgressGate gate : MvpProgressTracker.gates()) {
            markdown
                .append("| ").append(gate.number())
                .append(" | ").append(gate.title())
                .append(" | ").append(gate.status())
                .append(" | ").append(gate.currentEvidence())
                .append(" | ").append(gate.nextAction())
                .append(" |\n");
        }
        markdown.append('\n');
        markdown.append("MVP remains NO_GO until physical Bluetooth, realtime audio, two-device PTT, privacy/export, and release gates pass.\n");
        return markdown.toString();
    }
}
