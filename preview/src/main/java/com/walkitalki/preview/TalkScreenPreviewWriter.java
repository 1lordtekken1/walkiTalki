package com.walkitalki.preview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TalkScreenPreviewWriter {
    private TalkScreenPreviewWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected output HTML path");
        }
        Path output = Path.of(args[0]);
        Files.createDirectories(output.getParent());
        Files.writeString(output, TalkScreenPreviewPage.renderDefaultScenarios());
        System.out.println("Wrote talk screen preview to " + output);
    }
}
