package com.walkitalki.preview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PreviewAutomationAuditWriter {
    private PreviewAutomationAuditWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected output Markdown path");
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, PreviewAutomationAudit.auditDefaultScenarios().renderMarkdown());
        System.out.println("Wrote talk screen preview automation audit to " + output);
    }
}
