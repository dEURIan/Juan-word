package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MakeComparison {
    public static void main(String[] args) throws Exception {
        BufferedImage before = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794773907294_f51ec9a7.jpg"));
        BufferedImage after = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/blue_building_stage1_student.png"));

        int targetW = 640;
        int targetH = 360;

        BufferedImage comp = new BufferedImage(targetW * 2 + 30, targetH + 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = comp.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(new Color(15, 20, 35));
        g.fillRect(0, 0, comp.getWidth(), comp.getHeight());

        // Draw Before
        g.drawImage(before, 10, 45, targetW, targetH, null);
        g.setColor(new Color(255, 80, 80));
        g.drawRect(9, 44, targetW + 1, targetH + 1);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("BEFORE: Header & Bottom Tiles Overlapping Border", 15, 30);

        // Draw After
        g.drawImage(after, targetW + 20, 45, targetW, targetH, null);
        g.setColor(new Color(80, 220, 120));
        g.drawRect(targetW + 19, 44, targetW + 1, targetH + 1);
        g.drawString("AFTER: Properly Formatted Strictly Inside Border", targetW + 25, 30);

        g.dispose();

        File out = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/layout_containment_comparison.png");
        ImageIO.write(comp, "png", out);
        System.out.println("Saved comparison to " + out.getAbsolutePath());
    }
}
