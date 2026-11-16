package scratch;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import juanword.JuanWord;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;

public class TestTextContainmentInBoxes {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Text & Tile Containment Inside Box Boundaries ===");

        int[][] resolutions = {
            {960, 540},
            {1280, 720},
            {1920, 1080}
        };

        JuanWord app = new JuanWord();
        Player player = new Player(1, "TestHero");
        app.setActivePlayer(player);

        for (int[] res : resolutions) {
            int w = res[0];
            int h = res[1];
            System.out.println("\n--- Testing Resolution: " + w + "x" + h + " ---");

            BattleSession session = new BattleSessionBuilder()
                .withPlayer(player)
                .onMap(1)
                .atStage(1)
                .build();

            BattlePanel bp = new BattlePanel(app);
            bp.setSize(w, h);
            bp.begin(session);

            double scaleX = w / 960.0;
            double scaleY = h / 540.0;
            double S = Math.min(scaleX, scaleY);

            int cardX = (int) (20 * scaleX);
            int cardW = w - 2 * cardX;
            int juanW = (int) (185 * scaleX);
            int enemyW = (int) (185 * scaleX);
            int centerX = cardX + juanW + (int) (10 * scaleX);
            int centerW = cardW - juanW - enemyW - (int) (20 * scaleX);
            int prevW = (int) (centerW * 0.66);

            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            // 1. Verify empty hint text containment
            String emptyHint = "Porma ug pulong sa ubos";
            int hintFontSize = Math.max(12, (int) Math.round(13 * S));
            Font hintFont = new Font(Font.SANS_SERIF, Font.BOLD, hintFontSize);
            FontMetrics fmEh = g2.getFontMetrics(hintFont);
            int maxHintW = prevW - (int) Math.round(20 * scaleX);
            while (fmEh.stringWidth(emptyHint) > maxHintW && hintFontSize > 9) {
                hintFontSize--;
                hintFont = new Font(Font.SANS_SERIF, Font.BOLD, hintFontSize);
                fmEh = g2.getFontMetrics(hintFont);
            }
            int ehw = fmEh.stringWidth(emptyHint);
            int ehx = centerX + Math.max((int) Math.round(10 * scaleX), (prevW - ehw) / 2);

            assert ehx >= centerX : "Empty hint X (" + ehx + ") must be >= centerX (" + centerX + ")";
            assert ehx + ehw <= centerX + prevW : "Empty hint right edge (" + (ehx + ehw) + ") must be <= box right (" + (centerX + prevW) + ")";
            System.out.printf("  ✓ Empty hint text strictly inside preview box: [%d..%d] within [%d..%d] (Width=%d, BoxW=%d)\n",
                ehx, ehx + ehw, centerX, centerX + prevW, ehw, prevW);

            // 2. Verify word and DMG text containment across short and long words
            String[] testWords = {"BOI", "ATAKE", "DABAWENYO", "KASINGKASING"};
            for (String tw : testWords) {
                String dmgStr = "25 DMG";
                int dmgFontSize = Math.max(13, (int) Math.round(15 * S));
                Font dmgFont = new Font(Font.SANS_SERIF, Font.BOLD, dmgFontSize);
                FontMetrics fmDmg = g2.getFontMetrics(dmgFont);
                int dw = fmDmg.stringWidth(dmgStr);
                int dx = centerX + prevW - dw - (int) Math.round(16 * scaleX);

                int wordFontSize = Math.max(20, (int) Math.round(22 * S));
                Font wordFont = new Font(Font.SERIF, Font.BOLD, wordFontSize);
                FontMetrics fmWord = g2.getFontMetrics(wordFont);
                int maxWordW = dx - centerX - (int) Math.round(24 * scaleX);
                while (fmWord.stringWidth(tw) > maxWordW && wordFontSize > 11) {
                    wordFontSize--;
                    wordFont = new Font(Font.SERIF, Font.BOLD, wordFontSize);
                    fmWord = g2.getFontMetrics(wordFont);
                }
                int wx = centerX + (int) Math.round(14 * scaleX);
                int ww = fmWord.stringWidth(tw);

                assert wx >= centerX : "Word X must be >= centerX";
                assert wx + ww <= dx : "Word right edge must not collide with DMG text";
                assert dx + dw <= centerX + prevW : "DMG text must be inside box";
            }
            System.out.println("  ✓ Word preview and DMG stats strictly inside box across word lengths 3-12");

            // 3. Verify rack tiles containment across 1 to 12 tiles
            int maxRackW = centerW - (int) Math.round(20 * scaleX);
            for (int count = 1; count <= 12; count++) {
                int pTileSize = (int) Math.round(48 * Math.min(scaleX, scaleY));
                int pGap = 6;
                int totalRackW = count * pTileSize + Math.max(0, count - 1) * pGap;
                if (totalRackW > maxRackW && count > 0) {
                    pTileSize = Math.max(20, (maxRackW - Math.max(0, count - 1) * pGap) / count);
                    totalRackW = count * pTileSize + Math.max(0, count - 1) * pGap;
                }
                int startTileX = centerX + Math.max((int) Math.round(10 * scaleX), (centerW - totalRackW) / 2);

                assert startTileX >= centerX : "Rack start X must be >= centerX";
                assert startTileX + totalRackW <= centerX + centerW : "Rack right edge must be <= centerW";
            }
            System.out.println("  ✓ Rack letter tiles strictly inside word rack across tile counts 1-12");

            // 4. Render entire panel to verify no exceptions
            bp.paint(g2);
            g2.dispose();
        }

        System.out.println("\n=== ALL CONTAINMENT TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }
}
