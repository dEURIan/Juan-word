package scratch;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import juanword.JuanWord;
import juanword.MapSelectPanel;
import juanword.Player;

public class TestMapBannerAndLockVisuals {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        Player juan = new Player("P001", "Juan");
        juan.unlockedMapIndex = 1; // Only Blue Building unlocked
        app.setCurrentPlayer(juan);
        
        MapSelectPanel panel = new MapSelectPanel(app);
        panel.setSize(960, 540);
        panel.doLayout();
        
        // Let's create an image and render the screen
        BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        panel.paint(g2);
        g2.dispose();
        
        ImageIO.write(img, "png", new File("scratch/current_map_panel_render.png"));
        System.out.println("Saved current_map_panel_render.png");
        System.exit(0);
    }
}
