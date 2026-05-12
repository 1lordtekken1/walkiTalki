package com.walkitalki.core.simulation;

import java.util.Comparator;
import java.util.List;

public final class ReadinessReportRenderer {
    private ReadinessReportRenderer() {
    }

    public static String renderMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Executable readiness scorecard\n\n");
        markdown.append(ReadinessScorecard.summary()).append("\n\n");
        appendTable(markdown, "Module readiness", ReadinessScorecard.modules());
        appendTable(markdown, "Stakeholder readiness", ReadinessScorecard.stakeholders());
        appendTopBlockers(markdown, ReadinessScorecard.modules());
        return markdown.toString();
    }

    private static void appendTable(StringBuilder markdown, String heading, List<ReadinessScore> scores) {
        markdown.append("## ").append(heading).append("\n\n");
        markdown.append("| Name | Score | Current evidence | Path to 100 |\n");
        markdown.append("|---|---:|---|---|\n");
        for (ReadinessScore score : scores) {
            markdown
                .append("| ").append(score.name())
                .append(" | ").append(score.score())
                .append(" | ").append(score.currentEvidence())
                .append(" | ").append(score.pathTo100())
                .append(" |\n");
        }
        markdown.append('\n');
    }

    private static void appendTopBlockers(StringBuilder markdown, List<ReadinessScore> modules) {
        markdown.append("## Top blockers\n\n");
        modules.stream()
            .sorted(Comparator.comparingInt(ReadinessScore::score))
            .limit(3)
            .forEach(score -> markdown
                .append("- ").append(score.name())
                .append(" (").append(score.score()).append("/100): ")
                .append(score.pathTo100())
                .append('\n'));
    }
}
