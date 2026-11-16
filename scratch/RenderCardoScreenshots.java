package scratch;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.Player;
import juanword.ui.MapSelectPanel;
import juanword.ui.StartPanel;

public class RenderCardoScreenshots {
    public static void main(String[] args) throws Exception {
        DataManager dm = DataManager.getInstance();
        Player cardo = null;
        for (Player p : dm.getAllPlayers()) {
            if ("Cardo".equalsIgnoreCase(p.getPlayerName())) {
                cardo = p;
                break;
            }
        }
        if (cardo == null) {
            throw new IllegalStateException("Cardo player not found in DataManager!");
        }

        JuanWord app = new JuanWord();
        app.setActivePlayer(cardo);

        // 1. Render MapSelectPanel for Cardo
        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();
        mapPanel.doLayout();
        BufferedImage mapImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMap = mapImg.createGraphics();
        mapPanel.paint(gMap);
        gMap.dispose();
        File mapOut = new File("scratch/restored_map_cardo.png");
        ImageIO.write(mapImg, "PNG", mapOut);
        System.out.println("Rendered: " + mapOut.getAbsolutePath());

        // 2. Render StartPanel for Cardo
        StartPanel startPanel = new StartPanel(app);
        startPanel.setSize(960, 540);
        startPanel.doLayout();
        BufferedImage startImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gStart = startImg.createGraphics();
        startPanel.paint(gStart);
        gStart.dispose();
        File startOut = new File("scratch/restored_start_panel_cardo.png");
        ImageIO.write(startImg, "PNG", startOut);
        System.out.println("Rendered: " + startOut.getAbsolutePath());
    }
}
