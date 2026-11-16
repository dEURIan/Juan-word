package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;

public class RenderAnnotatedMapSelect {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        MapSelectPanel panel = new MapSelectPanel(app);
        
        Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
        animField.setAccessible(true);
        Field boundsField = MapSelectPanel.class.getDeclaredField("landmarkBounds");
        boundsField.setAccessible(true);
        
        int w = 960;
        int h = 540;
        
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.setSize(w, h);
        panel.setSize(w, h);
        panel.doLayout();
        animField.setFloat(panel, 1.0f);
        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        panel.paint(g2);
        
        Rectangle[] bounds = (Rectangle[]) boundsField.get(panel);
        Rectangle bb = bounds[MapSelectPanel.LANDMARK_BLUE_BUILDING];
        Rectangle dm = bounds[MapSelectPanel.LANDMARK_DURIAN_MUSEUM];
        Rectangle sp = bounds[MapSelectPanel.LANDMARK_SAN_PEDRO];
        
        // Draw debug overlay bounding boxes
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0, 255, 255, 200));
        g2.drawRect(bb.x, bb.y, bb.width, bb.height);
        g2.drawString("Blue Building: " + bb.width + "x" + bb.height, bb.x + 5, bb.y + 15);
        
        g2.setColor(new Color(255, 255, 0, 200));
        g2.drawRect(dm.x, dm.y, dm.width, dm.height);
        g2.drawString("Davao Museum: " + dm.width + "x" + dm.height, dm.x + 5, dm.y + 15);
        
        g2.setColor(new Color(255, 50, 50, 200));
        g2.drawRect(sp.x, sp.y, sp.width, sp.height);
        g2.drawString("San Pedro: " + sp.width + "x" + sp.height, sp.x + 5, sp.y + 15);
        
        g2.dispose();
        ImageIO.write(img, "png", new File("scratch/annotated_map_select.png"));
        ImageIO.write(img, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/annotated_map_select.png"));
        System.out.println("Annotated map select written to scratch/annotated_map_select.png");
        System.exit(0);
    }
}
