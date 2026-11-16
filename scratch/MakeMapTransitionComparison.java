package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MakeMapTransitionComparison {
    public static void main(String[] args) throws Exception {
        BufferedImage mid = ImageIO.read(new File("scratch/map_transition_in_progress.png"));
        BufferedImage arrived = ImageIO.read(new File("scratch/map_transition_arrived.png"));

        int targetW = 640;
        int targetH = 360;

        BufferedImage comp = new BufferedImage(targetW * 2 + 30, targetH + 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = comp.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(new Color(10, 14, 38));
        g.fillRect(0, 0, comp.getWidth(), comp.getHeight());

        // 1. Draw Mid-Transition
        g.drawImage(mid, 10, 45, targetW, targetH, null);
        g.setColor(new Color(245, 184, 32));
        g.drawRect(9, 44, targetW + 1, targetH + 1);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(245, 184, 32));
        g.drawString("1. TRAVELING: Blue Building -> Left | Davao Museum <- Right", 15, 30);

        // 2. Draw Arrived
        g.drawImage(arrived, targetW + 20, 45, targetW, targetH, null);
        g.setColor(new Color(80, 220, 120));
        g.drawRect(targetW + 19, 44, targetW + 1, targetH + 1);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(80, 220, 120));
        g.drawString("2. ARRIVED: Settled in Davao Museum (Stage 1 Ready)", targetW + 25, 30);

        g.dispose();

        File out = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_travel_transition_comparison.png");
        ImageIO.write(comp, "png", out);
        System.out.println("Saved comparison to " + out.getAbsolutePath());
    }
}
