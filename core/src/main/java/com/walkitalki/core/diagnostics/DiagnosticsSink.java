package com.walkitalki.core.diagnostics;

public interface DiagnosticsSink {
    void record(DiagnosticsEvent event);
}
