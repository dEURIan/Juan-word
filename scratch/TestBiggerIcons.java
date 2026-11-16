package scratch;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class TestBiggerIcons {
    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        JuanWord app = new JuanWord();
        Player p = new Player(1, "Cardo");
        p.setUnlockedMap(4);
        app.setActivePlayer(p);

        MapSelectPanel panel = new MapSelectPanel(app);
        panel.setSize(960, 540);
        panel.refresh();
        panel.doLayout();

        BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();

        ImageIO.write(img, "png", new File("scratch/preview_bigger_icons.png"));
        System.out.println("Preview saved to scratch/preview_bigger_icons.png");

        for (int i = 0; i < panel.getLandmarks().size(); i++) {
            Landmark lm = panel.getLandmarks().get(i);
            Rectangle b = lm.getBounds();
            System.out.printf("Map %d bounds: [x=%d, y=%d, w=%d, h=%d]\n", i+1, b.x, b.y, b.width, b.height);
        }
        System.exit(0);
    }
}
