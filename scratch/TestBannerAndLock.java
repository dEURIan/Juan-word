import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestBannerAndLock {
    public static void main(String[] args) throws Exception {
        int w = 960;
        int h = 540;
        
        // Let's test different banner designs
        // Maps:
        // 1. Blue Building - Easy
        // 2. Davao Museum - Medium
        // 3. San Pedro Cathedral - Hard
        // 4. Mt. Apo - Extreme
        
        String[][] maps = {
            {"BLUE BUILDING", "EASY"},
            {"DAVAO MUSEUM", "MEDIUM"},
            {"SAN PEDRO CATHEDRAL", "HARD"},
            {"MT. APO", "EXTREME"}
        };
        
        Color[] diffColors = {
            new Color(110, 230, 130), // Easy - Green
            new Color(245, 190, 75),  // Medium - Gold
            new Color(255, 120, 90),  // Hard - Coral / Red-Orange
            new Color(190, 130, 255)  // Extreme - Purple
        };

        // Design A: Single line "MAP NAME • DIFFICULTY"
        // Design B: Single line "MAP NAME - DIFFICULTY"
        // Design C: Two lines: Map Name (large) / Difficulty (sleek pill or sub)
        
        for (int i = 0; i < 4; i++) {
            BufferedImage img = new BufferedImage(500, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Design A: Single line compact banner
            int bw = 400;
            int bh = 42;
            int bx = 50;
            int by = 30;
            
            g2.setColor(new Color(15, 20, 36, 235));
            g2.fillRoundRect(bx, by, bw, bh, 14, 14);
            g2.setColor(diffColors[i]);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(bx, by, bw, bh, 14, 14);
            
            String line = maps[i][0] + "  •  " + maps[i][1];
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(Color.WHITE);
            int tw = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, bx + (bw - tw) / 2, by + 26);
            
            g2.dispose();
            ImageIO.write(img, "png", new File("scratch/banner_single_" + i + ".png"));
        }
        
        System.out.println("Rendered banner designs.");
    }
}
