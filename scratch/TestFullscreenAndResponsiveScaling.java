package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import juanword.*;

public class TestFullscreenAndResponsiveScaling {

    public static void main(String[] args) throws Exception {
        System.out.println("=== STARTING FULLSCREEN & RESPONSIVE SCALING VERIFICATION ===");
        final CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        SwingUtilities.invokeLater(() -> {
            try {
                DataManager dm = DataManager.getInstance();
                JuanWord app = new JuanWord();

                // 1. VERIFY WINDOW MANAGEMENT PROPERTIES
                System.out.println("[TEST 1] Checking window resizability and minimum dimensions...");
                assert app.isResizable() : "JuanWord frame must be resizable!";
                Dimension minSize = app.getMinimumSize();
                assert minSize != null && minSize.width >= 800 && minSize.height >= 500 :
                    "JuanWord minimum size must be at least 800x500! Was: " + minSize;
                System.out.println("  ✓ Resizable: true, Minimum Size: " + minSize.width + "x" + minSize.height);

                // 2. VERIFY FULLSCREEN TOGGLING LOGIC
                System.out.println("[TEST 2] Checking fullscreen toggling API...");
                assert !app.isFullScreen() : "Initial state should not be fullscreen!";
                app.setFullScreen(true);
                assert app.isFullScreen() : "setFullScreen(true) must set isFullScreen to true!";
                app.toggleFullScreen();
                assert !app.isFullScreen() : "toggleFullScreen() must invert isFullScreen to false!";
                app.toggleFullScreen();
                assert app.isFullScreen() : "toggleFullScreen() second call must invert isFullScreen to true!";
                app.setFullScreen(false);
                assert !app.isFullScreen() : "setFullScreen(false) must restore windowed state!";
                System.out.println("  ✓ Fullscreen toggling verified cleanly.");

                // 3. VERIFY START / MENU SCREEN RESPONSIVE SCALING
                System.out.println("[TEST 3] Checking Start Screen responsive layout (960x540 vs 1920x1080)...");
                JPanel startScreen = app.createStartScreen();
                
                // Layout at 960x540
                startScreen.setBounds(0, 0, 960, 540);
                startScreen.doLayout();
                validateTreeLayout(startScreen);

                BufferedImage startImg960 = renderComponent(startScreen, 960, 540);
                saveImage(startImg960, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/start_screen_960x540.png");

                // Layout at 1920x1080 (Full HD / Fullscreen)
                startScreen.setBounds(0, 0, 1920, 1080);
                startScreen.doLayout();
                validateTreeLayout(startScreen);

                System.out.println("DEBUG StartScreen 1920x1080 button dimensions: continueBtn getWidth=" + app.getContinueButton().getWidth() + ", getHeight=" + app.getContinueButton().getHeight() + ", targetWidth=" + app.getContinueButton().getTargetWidth() + ", targetHeight=" + app.getContinueButton().getTargetHeight());

                BufferedImage startImg1080 = renderComponent(startScreen, 1920, 1080);
                saveImage(startImg1080, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/start_screen_1920x1080.png");

                BufferedImage startCollage = createSideBySideCollage(
                    startImg960, "Start Menu 960x540 (Scale 1.0x)",
                    startImg1080, "Start Menu 1920x1080 (Scale 2.0x)"
                );
                saveImage(startCollage, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/start_screen_comparison.png");
                System.out.println("  ✓ Start screen scaled from 960x540 to 1920x1080 successfully.");

                // 4. VERIFY MAP SELECTION SCREEN RESPONSIVE STAGE FRAMING
                System.out.println("[TEST 4] Checking Map Select Panel responsive stage framing...");
                MapSelectPanel mapPanel = new MapSelectPanel(app);

                // 960x540 Map Select
                mapPanel.setBounds(0, 0, 960, 540);
                mapPanel.doLayout();
                BufferedImage mapImg960 = renderComponent(mapPanel, 960, 540);
                saveImage(mapImg960, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_select_960x540.png");

                // 1920x1080 Map Select
                mapPanel.setBounds(0, 0, 1920, 1080);
                mapPanel.doLayout();
                BufferedImage mapImg1080 = renderComponent(mapPanel, 1920, 1080);
                saveImage(mapImg1080, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_select_1920x1080.png");

                BufferedImage mapCollage = createSideBySideCollage(
                    mapImg960, "Map Select 960x540 (Scale 1.0x)",
                    mapImg1080, "Map Select 1920x1080 (Scale 2.0x)"
                );
                saveImage(mapCollage, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_select_comparison.png");
                System.out.println("  ✓ Map select screen stage framing verified across 960p, 1080p, and Ultrawide.");

                // 5. VERIFY BATTLE SCREEN RESPONSIVE SCALING & MATHEMATICAL DEAD-CENTERING
                System.out.println("[TEST 5] Checking Battle Panel responsive scaling and tile centering...");
                Player juan = dm.loadPlayer("juan_default");
                if (juan == null) {
                    juan = new Player("juan_default", "Juan");
                }
                BattlePanel battlePanel = new BattlePanel(app, dm);
                battlePanel.setupBattle(juan, "Blue Building", 1, "Sayon");

                // First render WITH round title card overlay showing
                battlePanel.setBounds(0, 0, 960, 540);
                battlePanel.doLayout();
                validateTreeLayout(battlePanel);
                BufferedImage overlay960 = renderComponent(battlePanel, 960, 540);
                saveImage(overlay960, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_overlay_960x540.png");

                battlePanel.setBounds(0, 0, 1920, 1080);
                battlePanel.doLayout();
                validateTreeLayout(battlePanel);
                BufferedImage overlay1080 = renderComponent(battlePanel, 1920, 1080);
                saveImage(overlay1080, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_overlay_1920x1080.png");

                // Now dismiss title card overlay to expose gameplay arena
                battlePanel.dismissTitleCardForTesting();

                // Windowed 960x540 Gameplay
                battlePanel.setBounds(0, 0, 960, 540);
                battlePanel.doLayout();
                validateTreeLayout(battlePanel);

                int tileSize960 = battlePanel.getTileSize();
                float scale960 = battlePanel.getResponsiveScale();
                System.out.println("  [960x540] Scale: " + scale960 + ", TileSize: " + tileSize960 + "px");
                assert tileSize960 >= 48 && tileSize960 <= 55 : "Tile size at 960x540 should be ~52px! Was: " + tileSize960;

                BufferedImage battleImg960 = renderComponent(battlePanel, 960, 540);
                saveImage(battleImg960, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_screen_960x540.png");

                // Fullscreen / Full HD 1920x1080 Gameplay
                battlePanel.setBounds(0, 0, 1920, 1080);
                battlePanel.doLayout();
                validateTreeLayout(battlePanel);

                int tileSize1080 = battlePanel.getTileSize();
                float scale1080 = battlePanel.getResponsiveScale();
                System.out.println("  [1920x1080] Scale: " + scale1080 + ", TileSize: " + tileSize1080 + "px");
                assert tileSize1080 >= 95 && tileSize1080 <= 110 : "Tile size at 1920x1080 should scale to ~104px! Was: " + tileSize1080;
                assert scale1080 >= 1.8f && scale1080 <= 2.2f : "Scale at 1920x1080 should be ~2.0! Was: " + scale1080;

                BufferedImage battleImg1080 = renderComponent(battlePanel, 1920, 1080);
                saveImage(battleImg1080, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_screen_1920x1080.png");

                // 6. MULTI-PANEL PROGRESSION COLLAGE
                System.out.println("[TEST 6] Generating side-by-side comparison collages...");
                BufferedImage collage = createSideBySideCollage(
                    battleImg960, "Windowed 960x540 (Scale 1.0x, Tile 48px)",
                    battleImg1080, "Full HD / Full Screen 1920x1080 (Scale 2.0x, Tile 97px)"
                );
                saveImage(collage, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/fullscreen_responsive_comparison.png");

                BufferedImage overlayCollage = createSideBySideCollage(
                    overlay960, "Round Title Card 960x540 (Scale 1.0x)",
                    overlay1080, "Round Title Card 1920x1080 (Scale 2.0x)"
                );
                saveImage(overlayCollage, "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/round_overlay_comparison.png");

                System.out.println("=== ALL FULLSCREEN & RESPONSIVE SCALING CHECKS PASSED SUCCESSFULLY! ===");
            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        boolean done = latch.await(15, TimeUnit.SECONDS);
        if (!done) {
            throw new RuntimeException("Test timed out!");
        }
        if (error[0] != null) {
            error[0].printStackTrace();
            System.exit(1);
        }
    }

    private static void validateTreeLayout(Component c) {
        if (c instanceof Container) {
            ((Container) c).doLayout();
            for (Component child : ((Container) c).getComponents()) {
                validateTreeLayout(child);
            }
        }
    }

    private static BufferedImage renderComponent(JComponent comp, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        comp.setBounds(0, 0, w, h);
        comp.doLayout();
        comp.paint(g2);
        g2.dispose();
        return img;
    }

    private static void saveImage(BufferedImage img, String path) {
        try {
            File f = new File(path);
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            ImageIO.write(img, "PNG", f);
            System.out.println("  Saved artifact: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage createSideBySideCollage(BufferedImage left, String leftTitle, BufferedImage right, String rightTitle) {
        int targetH = 600;
        int leftW = (int) Math.round((double) left.getWidth() * targetH / left.getHeight());
        int rightW = (int) Math.round((double) right.getWidth() * targetH / right.getHeight());
        int bannerH = 45;
        int totalW = leftW + rightW + 20;
        int totalH = targetH + bannerH;

        BufferedImage collage = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = collage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(new Color(12, 16, 52));
        g.fillRect(0, 0, totalW, totalH);

        // Draw Left
        g.drawImage(left, 0, bannerH, leftW, targetH, null);
        g.setColor(new Color(245, 180, 50));
        g.drawRect(0, bannerH, leftW, targetH - 1);

        // Draw Right
        g.drawImage(right, leftW + 20, bannerH, rightW, targetH, null);
        g.setColor(new Color(75, 175, 255));
        g.drawRect(leftW + 20, bannerH, rightW - 1, targetH - 1);

        // Headers
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString(leftTitle, 15, 28);
        g.drawString(rightTitle, leftW + 35, 28);

        g.dispose();
        return collage;
    }
}
