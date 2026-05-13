package com.walkitalki.core.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StakeholderRedTeamReviewWriter {
    private StakeholderRedTeamReviewWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: StakeholderRedTeamReviewWriter <output-file>");
        }
        Path output = Path.of(args[0]);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, StakeholderRedTeamReview.current().renderMarkdown());
        System.out.println("Wrote stakeholder red-team review to " + output.toAbsolutePath());
    }
}
