package scratch;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.Player;
import juanword.ui.MapSelectPanel;
import juanword.ui.StartPanel;

public class RenderGudoUnlockedScreens {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering Gudo Unlocked Screens...");
        File artifactDir = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5");

        DataManager dm = DataManager.getInstance();
        Player gudo = null;
        for (Player p : dm.getAllPlayers()) {
            if ("Gudo".equalsIgnoreCase(p.getPlayerName())) {
                gudo = p;
                break;
            }
        }
        assert gudo != null;

        JuanWord app = new JuanWord();
        app.setActivePlayer(gudo);

        // 1. Start Screen with Gudo Profile Active
        StartPanel startPanel = new StartPanel(app);
        startPanel.setSize(960, 540);
        BufferedImage startImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2S = startImg.createGraphics();
        g2S.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        startPanel.paint(g2S);
        g2S.dispose();
        ImageIO.write(startImg, "png", new File(artifactDir, "gudo_start_screen_unlocked.png"));
        System.out.println("  ✓ Rendered gudo_start_screen_unlocked.png");

        // 2. Map Screen with Gudo Profile Active (All 4 maps unlocked + 'Fight Gudo' challenge button)
        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();
        BufferedImage mapImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2M = mapImg.createGraphics();
        g2M.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mapPanel.paint(g2M);
        g2M.dispose();
        ImageIO.write(mapImg, "png", new File(artifactDir, "gudo_map_screen_unlocked.png"));
        System.out.println("  ✓ Rendered gudo_map_screen_unlocked.png");

        System.out.println("Done!");
        System.exit(0);
    }
}
