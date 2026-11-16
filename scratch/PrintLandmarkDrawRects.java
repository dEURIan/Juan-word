package scratch;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class PrintLandmarkDrawRects {
    public static void main(String[] args) {
        JuanWord app = new JuanWord();
        Player p = new Player(1, "Cardo");
        p.setUnlockedMap(4);
        app.setActivePlayer(p);

        MapSelectPanel panel = new MapSelectPanel(app);
        panel.setSize(960, 540);
        panel.refresh();

        System.out.println("=== Current Landmark Layout at 960x540 ===");
        for (int i = 0; i < panel.getLandmarks().size(); i++) {
            Landmark lm = panel.getLandmarks().get(i);
            Rectangle b = lm.getBounds();
            System.out.printf("Landmark %d: Bounds=[x=%d, y=%d, w=%d, h=%d]\n", i+1, b.x, b.y, b.width, b.height);
        }
    }
}
