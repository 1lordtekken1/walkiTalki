import com.walkitalki.app.AppDebugHarness;
import com.walkitalki.app.AppDebugReport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class RenderDebugSmokeReport {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: RenderDebugSmokeReport <output-file>");
        }
        AppDebugReport report = AppDebugHarness.createMvpHarness().runMvpSmokeTest();
        File output = new File(args[0]);
        File parent = output.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create " + parent);
        }
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(report.renderMarkdown());
        }
        System.out.println("Wrote MVP debug smoke report to " + output.getAbsolutePath());
    }
}
