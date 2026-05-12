package com.walkitalki.core.quality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ArchitectureGuard {
    private static final List<String> PROTECTED_SOURCE_ROOTS = List.of(
        "core/src/main/java",
        "preview/src/main/java"
    );
    private static final List<String> FORBIDDEN_PATTERNS = List.of(
        "import android.",
        "import androidx.",
        "android.bluetooth.",
        "android.media.",
        "BluetoothSocket",
        "AudioRecord",
        "AudioTrack"
    );

    private ArchitectureGuard() {
    }

    public static Report scanRepository() {
        Path repositoryRoot = findRepositoryRoot();
        List<String> violations = new ArrayList<>();
        int checkedRoots = 0;
        for (String sourceRoot : PROTECTED_SOURCE_ROOTS) {
            Path root = repositoryRoot.resolve(sourceRoot);
            if (!Files.exists(root)) {
                continue;
            }
            checkedRoots++;
            scanRoot(repositoryRoot, root, violations);
        }
        return new Report(checkedRoots, List.copyOf(violations));
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find repository root containing settings.gradle");
    }

    private static void scanRoot(Path repositoryRoot, Path root, List<String> violations) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> scanFile(repositoryRoot, path, violations));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan architecture guard root: " + root, exception);
        }
    }

    private static void scanFile(Path repositoryRoot, Path file, List<String> violations) {
        try {
            String content = Files.readString(file);
            for (String forbiddenPattern : FORBIDDEN_PATTERNS) {
                if (content.contains(forbiddenPattern)) {
                    violations.add(repositoryRoot.relativize(file) + " contains forbidden framework reference: " + forbiddenPattern);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan architecture guard file: " + file, exception);
        }
    }

    public record Report(int checkedRoots, List<String> violations) {
        public Report {
            violations = List.copyOf(Objects.requireNonNull(violations, "violations"));
        }

        public boolean clean() {
            return violations.isEmpty();
        }

        public String summary() {
            return "checked=" + checkedRoots + " violations=" + violations.size();
        }
    }
}
