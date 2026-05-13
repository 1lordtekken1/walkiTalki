package com.walkitalki.app;

import java.util.List;

public final class AppDebugReport {
    private final List<AppDebugStep> steps;
    private final AppDiagnosticsReport supportReport;

    AppDebugReport(List<AppDebugStep> steps, AppDiagnosticsReport supportReport) {
        this.steps = List.copyOf(steps);
        this.supportReport = supportReport;
    }

    public boolean passed() {
        return steps.size() == 5
                && "SCAN".equals(steps.get(0).buttonLabel())
                && "...".equals(steps.get(1).buttonLabel())
                && "PTT".equals(steps.get(2).buttonLabel())
                && "TALK".equals(steps.get(3).buttonLabel())
                && "PTT".equals(steps.get(4).buttonLabel())
                && steps.get(3).transmitting()
                && steps.get(4).pushToTalkEnabled();
    }

    public List<AppDebugStep> steps() {
        return steps;
    }

    public String renderMarkdown() {
        StringBuilder builder = new StringBuilder();
        builder.append("# walkiTalki debug smoke test\n");
        builder.append("result=").append(passed() ? "PASS" : "FAIL").append('\n');
        builder.append("redaction=enabled\n");
        builder.append("steps:\n");
        for (int index = 0; index < steps.size(); index++) {
            AppDebugStep step = steps.get(index);
            builder.append("- ").append(index + 1).append(". ")
                    .append(step.action())
                    .append(" -> ")
                    .append(step.buttonLabel())
                    .append(" [")
                    .append(step.diagnosticsSignal())
                    .append("]\n");
        }
        builder.append('\n').append(supportReport.renderMarkdown());
        return builder.toString();
    }
}
