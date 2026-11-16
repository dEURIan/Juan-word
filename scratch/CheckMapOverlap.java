package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;

public class CheckMapOverlap {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        MapSelectPanel panel = new MapSelectPanel(app);
        
        Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
        animField.setAccessible(true);
        Field boundsField = MapSelectPanel.class.getDeclaredField("landmarkBounds");
        boundsField.setAccessible(true);
        
        int[] widths = {960, 1024, 1280, 1920};
        int[] heights = {540, 665, 720, 1080};
        
        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            int h = heights[i];
            
            JFrame frame = new JFrame();
            frame.setContentPane(panel);
            frame.setSize(w, h);
            panel.setSize(w, h);
            panel.doLayout();
            animField.setFloat(panel, 1.0f);
            
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            panel.paint(g2);
            g2.dispose();
            
            Rectangle[] bounds = (Rectangle[]) boundsField.get(panel);
            Rectangle bb = bounds[MapSelectPanel.LANDMARK_BLUE_BUILDING];
            Rectangle dm = bounds[MapSelectPanel.LANDMARK_DURIAN_MUSEUM];
            Rectangle sp = bounds[MapSelectPanel.LANDMARK_SAN_PEDRO];
            
            System.out.println("=== Resolution: " + w + "x" + h + " ===");
            System.out.printf("Blue Building:  x=%d, w=%d, right=%d, y=%d, h=%d\n", bb.x, bb.width, bb.x + bb.width, bb.y, bb.height);
            System.out.printf("Davao Museum:   x=%d, w=%d, right=%d, y=%d, h=%d\n", dm.x, dm.width, dm.x + dm.width, dm.y, dm.height);
            System.out.printf("San Pedro:      x=%d, w=%d, right=%d, y=%d, h=%d\n", sp.x, sp.width, sp.x + sp.width, sp.y, sp.height);
            
            int bbDmOverlap = (bb.x + bb.width) - dm.x;
            int dmSpOverlap = (dm.x + dm.width) - sp.x;
            System.out.printf("BB & DM overlap: %d px (%s)\n", bbDmOverlap, bbDmOverlap > 0 ? "OVERLAPPING!" : "Gap of " + (-bbDmOverlap) + "px");
            System.out.printf("DM & SP overlap: %d px (%s)\n", dmSpOverlap, dmSpOverlap > 0 ? "OVERLAPPING!" : "Gap of " + (-dmSpOverlap) + "px");
            
            if (i == 0) {
                ImageIO.write(img, "png", new File("scratch/current_map_select_960x540.png"));
                ImageIO.write(img, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/current_map_select_960x540.png"));
            }
            frame.dispose();
        }
        System.exit(0);
    }
}
