package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderCharacterSpriteStrip {
    public static void main(String[] args) throws Exception {
        String artifactDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";

        BufferedImage f1 = ImageIO.read(new File(artifactDir, "character_anim_1_idle.png"));
        BufferedImage f2 = ImageIO.read(new File(artifactDir, "character_anim_2_preattack.png"));
        BufferedImage f3 = ImageIO.read(new File(artifactDir, "character_anim_3_attack.png"));
        BufferedImage f4 = ImageIO.read(new File(artifactDir, "character_anim_4_recovery.png"));

        // Crop Juan arena card area: x: 30 to 250, y: 60 to 270 (width: 220, height: 210)
        int cropX = 35;
        int cropY = 65;
        int cropW = 210;
        int cropH = 200;

        BufferedImage c1 = f1.getSubimage(cropX, cropY, cropW, cropH);
        BufferedImage c2 = f2.getSubimage(cropX, cropY, cropW, cropH);
        BufferedImage c3 = f3.getSubimage(cropX, cropY, cropW, cropH);
        BufferedImage c4 = f4.getSubimage(cropX, cropY, cropW, cropH);

        // Strip: 4 * 210 = 840W x 260H
        BufferedImage strip = new BufferedImage(840, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = strip.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dark background
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, 840, 260);

        BufferedImage[] crops = { c1, c2, c3, c4 };
        String[] titles = { "1. Idle (Breathing)", "2. Pre-Attack (Wind-up)", "3. Attack (Strike Lunge)", "4. Recovery (Return)" };

        for (int i = 0; i < 4; i++) {
            int x = i * 210;
            g.drawImage(crops[i], x, 30, null);

            // Border
            g.setColor(new Color(255, 195, 25, 120));
            g.drawRect(x + 2, 30, cropW - 4, cropH - 2);

            // Title banner
            g.setColor(new Color(255, 195, 25));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int strW = fm.stringWidth(titles[i]);
            g.drawString(titles[i], x + (cropW - strW) / 2, 20);
        }

        g.dispose();
        ImageIO.write(strip, "png", new File(artifactDir, "character_sprites_strip.png"));
        System.out.println("✓ Sprite progression strip generated!");
        System.exit(0);
    }
}
