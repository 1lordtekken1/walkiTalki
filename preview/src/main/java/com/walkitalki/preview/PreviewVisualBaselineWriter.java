package com.walkitalki.preview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PreviewVisualBaselineWriter {
    private PreviewVisualBaselineWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected output Markdown path");
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, PreviewVisualBaseline.captureDefaultScenarios().renderMarkdown());
        System.out.println("Wrote talk screen visual baseline to " + output);
    }
}
