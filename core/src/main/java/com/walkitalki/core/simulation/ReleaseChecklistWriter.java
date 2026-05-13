package com.walkitalki.core.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReleaseChecklistWriter {
    private ReleaseChecklistWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: ReleaseChecklistWriter <output-file>");
        }
        Path output = Path.of(args[0]);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, ReleaseChecklist.current().renderMarkdown());
        System.out.println("Wrote release checklist to " + output.toAbsolutePath());
    }
}
