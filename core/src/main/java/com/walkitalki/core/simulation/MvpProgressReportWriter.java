package com.walkitalki.core.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MvpProgressReportWriter {
    private MvpProgressReportWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: MvpProgressReportWriter <output-file>");
        }
        Path output = Path.of(args[0]);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, MvpProgressReportRenderer.renderMarkdown());
        System.out.println("Wrote MVP progress report to " + output.toAbsolutePath());
    }
}
