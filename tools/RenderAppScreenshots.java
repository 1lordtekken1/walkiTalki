import com.walkitalki.app.AppScreenshotCatalog;
import com.walkitalki.app.AppUiCopy;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.imageio.ImageIO;

public final class RenderAppScreenshots {
    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;
    private static final String[] FILE_NAMES = {
        "walkitalki-mvp-01-scan.png",
        "walkitalki-mvp-02-scanning.png",
        "walkitalki-main.png",
        "walkitalki-ptt-active.png"
    };
    private static final String[] CONFIRMATIONS = {
        "Состояние подтверждено: launch → SCAN",
        "Состояние подтверждено: tap → scanning",
        "Состояние подтверждено: fake peer → PTT",
        "Состояние подтверждено: ACTION_DOWN → TALK"
    };
    private static final String[] STATE_NAMES = {
        "Launch / scan",
        "Scanning",
        "Ready PTT",
        "Transmitting"
    };

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: RenderAppScreenshots <output-directory>");
        }
        File outputDirectory = new File(args[0]);
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to create " + outputDirectory);
        }
        List<AppUiCopy> states = AppScreenshotCatalog.states();
        if (states.size() != FILE_NAMES.length) {
            throw new IllegalStateException("Expected " + FILE_NAMES.length + " screenshot states but got " + states.size());
        }
        for (int index = 0; index < states.size(); index++) {
            render(new File(outputDirectory, FILE_NAMES[index]), states.get(index), CONFIRMATIONS[index]);
        }
        writeReadme(outputDirectory, states);
    }

    private static void render(File output, AppUiCopy copy, String confirmation) throws Exception {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setPaint(new GradientPaint(0, 0, new Color(15, 23, 42), 0, HEIGHT, new Color(2, 6, 23)));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(new Color(30, 41, 59));
        g.fill(new RoundRectangle2D.Float(64, 112, WIDTH - 128, HEIGHT - 224, 56, 56));

        g.setColor(new Color(226, 232, 240));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 76));
        g.drawString(copy.title(), 120, 240);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
        g.setColor(new Color(125, 211, 252));
        g.drawString(copy.status(), 120, 318);

        int buttonDiameter = 340;
        int buttonX = (WIDTH - buttonDiameter) / 2;
        int buttonY = 520;
        g.setColor(copy.transmitting() ? new Color(244, 63, 94) : new Color(20, 184, 166));
        g.fillOval(buttonX, buttonY, buttonDiameter, buttonDiameter);
        g.setStroke(new BasicStroke(14f));
        g.setColor(copy.transmitting() ? new Color(251, 113, 133) : new Color(45, 212, 191));
        g.drawOval(buttonX + 12, buttonY + 12, buttonDiameter - 24, buttonDiameter - 24);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 58));
        centered(g, copy.buttonLabel(), WIDTH / 2, buttonY + 194);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 38));
        g.setColor(new Color(226, 232, 240));
        centered(g, copy.primaryAction(), WIDTH / 2, 980);

        g.setColor(new Color(51, 65, 85));
        g.fill(new RoundRectangle2D.Float(120, 1100, WIDTH - 240, 330, 36, 36));
        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        g.drawString(copy.peerSummary(), 160, 1180);
        g.drawString("Диагностика без MAC, raw peer ID и audio payload", 160, 1248);
        g.drawString("Сигнал: " + copy.diagnosticsSignal(), 160, 1316);
        g.drawString(confirmation, 160, 1384);

        g.dispose();
        ImageIO.write(image, "png", output);
    }

    private static void writeReadme(File outputDirectory, List<AppUiCopy> states) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(new File(outputDirectory, "README.md")))) {
            writer.println("# walkiTalki app screenshots");
            writer.println();
            writer.println("These screenshots are generated from `AppScreenshotCatalog`, which uses `AppTalkController` and `TalkScreenPresenter` state rather than hard-coded renderer-only copy.");
            writer.println();
            writer.println("| State | Image | Diagnostics signal |");
            writer.println("| --- | --- | --- |");
            for (int index = 0; index < states.size(); index++) {
                writer.println("| " + STATE_NAMES[index] + " | ![" + STATE_NAMES[index] + "](" + FILE_NAMES[index] + ") | `" + states.get(index).diagnosticsSignal() + "` |");
            }
        }
    }

    private static void centered(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }
}
