package com.walkitalki.core.diagnostics;

import java.util.ArrayList;
import java.util.List;

public final class RecordingDiagnosticsSink implements DiagnosticsSink {
    private final List<DiagnosticsEvent> events = new ArrayList<>();

    private RecordingDiagnosticsSink() {
    }

    public static RecordingDiagnosticsSink create() {
        return new RecordingDiagnosticsSink();
    }

    @Override
    public void record(DiagnosticsEvent event) {
        events.add(event);
    }

    public List<DiagnosticsEvent> events() {
        return List.copyOf(events);
    }

    public String exportRedacted() {
        return DiagnosticsRedactor.export(events());
    }
}
