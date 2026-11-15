package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderSpritesCheck {
    public static void main(String[] args) throws Exception {
        BufferedImage idle = ImageIO.read(new File("src/juanword/maincharacteridle.png"));
        BufferedImage pre = ImageIO.read(new File("src/juanword/maincharapreattack.png"));
        BufferedImage att = ImageIO.read(new File("src/juanword/maincharattack.png"));

        int frameW = 160;
        int frameH = 240;
        BufferedImage sheet = new BufferedImage(frameW * 3 + 80, frameH + 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Dark background matching battle arena
        g.setColor(new Color(24, 28, 48));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());

        // Baseline floor
        g.setColor(new Color(60, 70, 100));
        g.drawLine(20, frameH + 20, sheet.getWidth() - 20, frameH + 20);

        BufferedImage[] frames = {idle, pre, att};
        String[] labels = {"1. IDLE (maincharacteridle)", "2. PRE-ATTACK (maincharapreattack)", "3. ATTACK (maincharattack)"};

        for (int i = 0; i < 3; i++) {
            int x = 30 + i * (frameW + 20);
            int y = 20;

            // Draw shadow under character
            g.setColor(new Color(0, 0, 0, 80));
            g.fillOval(x + 20, frameH + 10, frameW - 40, 16);

            g.drawImage(frames[i], x, y, frameW, frameH, null);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int strW = fm.stringWidth(labels[i]);
            g.drawString(labels[i], x + (frameW - strW) / 2, frameH + 50);
        }
        g.dispose();

        File out = new File("scratch/juan_sprites_preview.png");
        ImageIO.write(sheet, "PNG", out);
        System.out.println("Wrote sprite preview to " + out.getAbsolutePath());
    }
}
