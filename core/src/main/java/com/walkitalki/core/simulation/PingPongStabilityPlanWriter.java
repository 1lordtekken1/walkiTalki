package com.walkitalki.core.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PingPongStabilityPlanWriter {
    private PingPongStabilityPlanWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PingPongStabilityPlanWriter <output-file>");
        }
        Path output = Path.of(args[0]);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, PingPongStabilityPlan.current().renderMarkdown());
        System.out.println("Wrote PING/PONG stability plan to " + output.toAbsolutePath());
    }
}
