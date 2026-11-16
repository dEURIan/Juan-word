package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class MakeMapOverlapComparison {
    public static void main(String[] args) throws Exception {
        BufferedImage before = ImageIO.read(new File("scratch/current_map_select_960x540.png"));
        
        JuanWord app = new JuanWord();
        MapSelectPanel panel = new MapSelectPanel(app);
        
        int w = 960;
        int h = 540;
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.setSize(w, h);
        panel.setSize(w, h);
        panel.doLayout();
        
        java.lang.reflect.Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
        animField.setAccessible(true);
        animField.setFloat(panel, 1.0f);
        
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gAfter = after.createGraphics();
        panel.paint(gAfter);
        gAfter.dispose();
        frame.dispose();
        
        // Composite comparison image
        int pad = 24;
        int bannerH = 70;
        int compW = w * 2 + pad * 3;
        int compH = h + bannerH + pad * 2;
        
        BufferedImage comp = new BufferedImage(compW, compH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = comp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setColor(new Color(15, 20, 32));
        g2.fillRect(0, 0, compW, compH);
        
        // Header
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(255, 215, 100));
        g2.drawString("JUANWORD: MAP SELECTION OVERLAP FIX COMPARISON", pad, 36);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(new Color(190, 200, 220));
        g2.drawString("Blue Building (Sayon), Davao Museum (Sakto), San Pedro Cathedral (Lisod) - 960x540 Baseline", pad, 58);
        
        // Draw Before (Overlapping)
        int x1 = pad;
        int y1 = bannerH + pad;
        g2.drawImage(before, x1, y1, w, h, null);
        g2.setColor(new Color(240, 70, 70));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRect(x1, y1, w, h);
        g2.setColor(new Color(240, 70, 70, 220));
        g2.fillRect(x1 + 10, y1 + 10, 430, 28);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("BEFORE: DM & SP Overlap 187px, SP Spills Out (Right=1010)", x1 + 16, y1 + 29);
        
        // Draw After (Resized Just Enough, Zero Overlap)
        int x2 = x1 + w + pad;
        g2.drawImage(after, x2, y1, w, h, null);
        g2.setColor(new Color(50, 215, 120));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRect(x2, y1, w, h);
        g2.setColor(new Color(30, 150, 80, 220));
        g2.fillRect(x2 + 10, y1 + 10, 480, 28);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("AFTER: Zero Overlap (50px Gaps), Contained Inside Frame (Right=928)", x2 + 16, y1 + 29);
        
        g2.dispose();
        
        File outLocal = new File("scratch/map_overlap_fix_comparison.png");
        ImageIO.write(comp, "png", outLocal);
        File outArtifact = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_overlap_fix_comparison.png");
        ImageIO.write(comp, "png", outArtifact);
        System.out.println("Comparison written to: " + outArtifact.getAbsolutePath());
        System.exit(0);
    }
}
