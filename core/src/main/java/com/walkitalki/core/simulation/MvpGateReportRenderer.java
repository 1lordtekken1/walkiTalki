package com.walkitalki.core.simulation;

public final class MvpGateReportRenderer {
    private MvpGateReportRenderer() {
    }

    public static String renderMarkdown() {
        MvpGateDecision decision = MvpGateEvaluator.evaluate();
        StringBuilder markdown = new StringBuilder();
        markdown.append("# MVP GO/NO-GO gate\n\n");
        markdown.append("Status: ").append(decision.status()).append("\n\n");
        markdown.append(decision.summary()).append("\n\n");
        appendList(markdown, "Blockers", decision.blockers());
        appendList(markdown, "Required evidence", decision.requiredEvidence());
        return markdown.toString();
    }

    private static void appendList(StringBuilder markdown, String heading, Iterable<String> values) {
        markdown.append("## ").append(heading).append("\n\n");
        for (String value : values) {
            markdown.append("- ").append(value).append('\n');
        }
        markdown.append('\n');
    }
}
