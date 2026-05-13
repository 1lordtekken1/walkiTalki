package com.walkitalki.app;

import java.util.List;

public final class AppDiagnosticsReport {
    private final List<String> timelineSignals;
    private final String finalStatus;
    private final String finalButtonLabel;

    AppDiagnosticsReport(List<String> timelineSignals, AppUiCopy finalCopy) {
        this.timelineSignals = List.copyOf(timelineSignals);
        this.finalStatus = finalCopy.status();
        this.finalButtonLabel = finalCopy.buttonLabel();
    }

    public String renderMarkdown() {
        StringBuilder builder = new StringBuilder();
        builder.append("# walkiTalki MVP diagnostics\n");
        builder.append("redaction=enabled\n");
        builder.append("finalStatus=").append(finalStatus).append('\n');
        builder.append("finalAction=").append(finalButtonLabel).append('\n');
        builder.append("timeline:\n");
        for (int index = 0; index < timelineSignals.size(); index++) {
            builder.append("- ").append(index + 1).append(':').append(' ')
                    .append(timelineSignals.get(index)).append('\n');
        }
        return builder.toString();
    }
}
