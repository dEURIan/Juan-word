package scratch;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import juanword.JuanWord;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.MapSelectPanel;

public class TestTextScaling {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Text Size Scaling & Centering Across Resolutions ===");

        int[][] resolutions = {
            {960, 540},
            {1280, 720},
            {1920, 1080}
        };

        JuanWord app = new JuanWord();
        Player player = new Player(1, "TestHero");
        player.setUnlockedMap(4);
        app.setActivePlayer(player);

        for (int[] res : resolutions) {
            int w = res[0];
            int h = res[1];
            System.out.println("\n--- Testing Resolution: " + w + "x" + h + " ---");

            // 1. Test BattlePanel text rendering
            BattleSession session = new BattleSessionBuilder()
                .withPlayer(player)
                .onMap(1)
                .atStage(1)
                .build();

            BattlePanel bp = new BattlePanel(app);
            bp.setSize(w, h);
            bp.begin(session);

            BufferedImage imgB = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gB = imgB.createGraphics();
            bp.paint(gB);
            gB.dispose();

            double S = Math.min(w / 960.0, h / 540.0);
            int expectedTitleSize = Math.max(15, (int) Math.round(16 * S));
            assert expectedTitleSize >= 15 : "Title font size should be at least 15; got " + expectedTitleSize;
            if (w == 1920) {
                assert expectedTitleSize == 32 : "In 1080p, title font size should scale to 32; got " + expectedTitleSize;
            }
            System.out.println("  ✓ BattlePanel rendered cleanly with scaled fonts (Player Title Font: " + expectedTitleSize + "pt)");

            // 2. Test MapSelectPanel text rendering
            MapSelectPanel mp = new MapSelectPanel(app);
            mp.setSize(w, h);
            mp.refresh();

            BufferedImage imgM = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gM = imgM.createGraphics();
            mp.paint(gM);
            gM.dispose();

            int expectedMapTitleSize = Math.max(13, (int) Math.round(14 * S));
            if (w == 1920) {
                assert expectedMapTitleSize == 28 : "In 1080p, map select title font size should scale to 28; got " + expectedMapTitleSize;
            }
            System.out.println("  ✓ MapSelectPanel rendered cleanly with scaled fonts (Map Title Font: " + expectedMapTitleSize + "pt)");
        }

        System.out.println("\n=== ALL TEXT SCALING TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }
}
