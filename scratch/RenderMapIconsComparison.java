package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderMapIconsComparison {
    public static void main(String[] args) throws Exception {
        String artifactDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";

        BufferedImage newMap = ImageIO.read(new File(artifactDir, "latest_map_select_bigger_icons.png"));
        BufferedImage oldMap = ImageIO.read(new File(artifactDir, "latest_map_select_swapped.png"));

        // Create a 960x1080 vertical comparison (Before vs After)
        BufferedImage comp = new BufferedImage(960, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = comp.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Top: Previous
        g.drawImage(oldMap, 0, 0, null);
        drawBanner(g, 30, 45, "BEFORE: Previous Landmark Icon Scale (San Pedro: 288x129, Durian: 217x248, Blue: 268x211)");

        // Bottom: New Bigger Icons
        g.drawImage(newMap, 0, 540, null);
        drawBanner(g, 30, 585, "AFTER: Enlarged Landmark Icons (+20% to +50% scale, San Pedro: 364x195, Durian: 260x297, Blue: 336x265)");

        // Divider
        g.setColor(new Color(255, 195, 25));
        g.setStroke(new BasicStroke(3.0f));
        g.drawLine(0, 540, 960, 540);

        g.dispose();
        ImageIO.write(comp, "png", new File(artifactDir, "map_icons_comparison.png"));
        System.out.println("Comparison saved to " + artifactDir + "/map_icons_comparison.png");
        System.exit(0);
    }

    private static void drawBanner(Graphics2D g, int x, int y, String text) {
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        g.setColor(new Color(15, 23, 42, 230));
        g.fillRoundRect(x - 10, y - 20, tw + 20, 28, 8, 8);
        g.setColor(new Color(255, 195, 25));
        g.drawRoundRect(x - 10, y - 20, tw + 20, 28, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }
}
