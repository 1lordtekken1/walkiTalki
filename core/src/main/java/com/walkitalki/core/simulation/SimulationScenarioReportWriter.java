package com.walkitalki.core.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SimulationScenarioReportWriter {
    private SimulationScenarioReportWriter() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one output path argument");
        }

        write(Path.of(args[0]));
    }

    public static void write(Path outputPath) {
        try {
            Path parent = outputPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputPath, SimulationScenarioReportRenderer.renderMarkdown());
            System.out.println("Wrote simulation scenario report to " + outputPath.toAbsolutePath());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write simulation scenario report: " + outputPath, exception);
        }
    }
}
