import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class TestMapSelectIntegration {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing 4-Map Architecture & Map Select Integration ===");

        // 1. Verify Player Juan Data & Progression Model
        DataManager dm = DataManager.getInstance();
        Player loadedJuan = dm.loadPlayer("P001");
        if (loadedJuan == null) {
            loadedJuan = new Player("P001", "Juan");
            loadedJuan.unlockedMapIndex = 1;
            dm.savePlayer(loadedJuan);
        }
        final Player juan = loadedJuan;
        assert juan.unlockedMapIndex == 1 : "Juan must only have Blue Building unlocked (unlockedMapIndex == 1), found: " + juan.unlockedMapIndex;
        assert juan.isMapUnlocked(1) : "Map 1 (Blue Building) must be unlocked";
        assert !juan.isMapUnlocked(2) : "Map 2 (Davao Museum) must be locked for Juan";
        assert !juan.isMapUnlocked(3) : "Map 3 (San Pedro Cathedral) must be locked for Juan";
        assert !juan.isMapUnlocked(4) : "Map 4 (Mt Apo) must be locked for Juan";
        System.out.println("  ✓ Player Juan data verified: only Blue Building is unlocked (unlockedMapIndex = 1).");

        // 2. Verify Predefined Difficulties
        assert "Sayon".equalsIgnoreCase(Player.getPredefinedDifficultyForMap("Blue Building")) : "Blue Building must be Sayon (Easy)";
        assert "Sakto".equalsIgnoreCase(Player.getPredefinedDifficultyForMap("Davao Museum")) : "Davao Museum must be Sakto (Medium)";
        assert "Lisod".equalsIgnoreCase(Player.getPredefinedDifficultyForMap("San Pedro Cathedral")) : "San Pedro must be Lisod (Hard)";
        assert "Extreme".equalsIgnoreCase(Player.getPredefinedDifficultyForMap("Mt Apo")) : "Mt Apo must be Extreme";
        System.out.println("  ✓ Predefined difficulties verified: Easy (Blue Building), Medium (Davao Museum), Hard (San Pedro), Extreme (Mt Apo).");

        // 3. Verify Map Progression Unlock Logic
        Player testProg = new Player("P_PROG", "ProgTester");
        testProg.unlockedMapIndex = 1;
        assert testProg.isMapUnlocked("Blue Building");
        assert !testProg.isMapUnlocked("Davao Museum");

        testProg.unlockNextMap(2);
        assert testProg.isMapUnlocked("Davao Museum");
        assert !testProg.isMapUnlocked("San Pedro");

        testProg.unlockNextMap(3);
        assert testProg.isMapUnlocked("San Pedro Cathedral");
        assert !testProg.isMapUnlocked("Mt Apo");

        testProg.unlockNextMap(4);
        assert testProg.isMapUnlocked("Mt Apo");
        System.out.println("  ✓ Map unlock progression verified across all 4 stages.");

        // 4. GUI & Staging Tests
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                game.setCurrentPlayer(juan);
                game.setSize(960, 540);
                game.doLayout();

                MapSelectPanel mapPanel = game.mapSelectPanel;
                assert mapPanel != null : "mapSelectPanel must not be null";

                mapPanel.setSize(960, 540);
                mapPanel.doLayout();

                // Animation Initiation
                mapPanel.startEntranceAnimation();
                System.out.println("  ✓ Entrance animation started successfully.");

                Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
                animField.setAccessible(true);

                // Frame at animProgress = 0.5 (rising)
                animField.setFloat(mapPanel, 0.5f);
                BufferedImage imgHalf = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gHalf = imgHalf.createGraphics();
                mapPanel.paint(gHalf);
                gHalf.dispose();
                ImageIO.write(imgHalf, "png", new File("scratch/map_anim_progress_50.png"));
                System.out.println("  ✓ Rendered frame at animProgress = 0.5 (rising up from bottom).");

                // Frame at animProgress = 1.0 (settled)
                animField.setFloat(mapPanel, 1.0f);
                BufferedImage imgFull = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gFull = imgFull.createGraphics();
                mapPanel.paint(gFull);
                gFull.dispose();
                ImageIO.write(imgFull, "png", new File("scratch/map_anim_progress_100.png"));
                System.out.println("  ✓ Rendered resting map select screen with locked silhouettes.");

                Field hoveredField = MapSelectPanel.class.getDeclaredField("hoveredLandmark");
                hoveredField.setAccessible(true);

                // Test Hover on Blue Building (Map 0, Left Foreground: x ~ 140, y ~ 420)
                MouseEvent hoverBB = new MouseEvent(mapPanel, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 140, 420, 0, false);
                mapPanel.getMouseMotionListeners()[0].mouseMoved(hoverBB);
                int hovered = hoveredField.getInt(mapPanel);
                assert hovered == MapSelectPanel.LANDMARK_BLUE_BUILDING : "Hovered landmark must be Blue Building (0), got: " + hovered;
                System.out.println("  ✓ Hover on Blue Building (index 0) detected with pixel-perfect hit-test.");

                // Render hover Blue Building preview
                BufferedImage imgHoverBB = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBB = imgHoverBB.createGraphics();
                mapPanel.paint(gBB);
                gBB.dispose();
                ImageIO.write(imgHoverBB, "png", new File("scratch/hover_blue_building_preview.png"));

                // Test Hover on Davao Museum (Map 1, Center Foreground: x ~ 460, y ~ 420)
                MouseEvent hoverDM = new MouseEvent(mapPanel, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 460, 420, 0, false);
                mapPanel.getMouseMotionListeners()[0].mouseMoved(hoverDM);
                hovered = hoveredField.getInt(mapPanel);
                assert hovered == MapSelectPanel.LANDMARK_DURIAN_MUSEUM : "Hovered landmark must be Davao Museum (1), got: " + hovered;
                System.out.println("  ✓ Hover on Davao Museum (index 1) detected with pixel-perfect hit-test.");

                // Render hover Davao Museum preview
                BufferedImage imgHoverDM = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gDM = imgHoverDM.createGraphics();
                mapPanel.paint(gDM);
                gDM.dispose();
                ImageIO.write(imgHoverDM, "png", new File("scratch/hover_locked_museum_preview.png"));

                // Test Hover on San Pedro Cathedral (Map 2, Right Foreground: x ~ 800, y ~ 420)
                MouseEvent hoverSP = new MouseEvent(mapPanel, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 800, 420, 0, false);
                mapPanel.getMouseMotionListeners()[0].mouseMoved(hoverSP);
                hovered = hoveredField.getInt(mapPanel);
                assert hovered == MapSelectPanel.LANDMARK_SAN_PEDRO : "Hovered landmark must be San Pedro (2), got: " + hovered;
                System.out.println("  ✓ Hover on San Pedro Cathedral (index 2) detected with pixel-perfect hit-test.");

                // Test Hover on Mt Apo (Map 3, Towering Background Summit: x ~ 480, y ~ 160)
                MouseEvent hoverApo = new MouseEvent(mapPanel, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 480, 160, 0, false);
                mapPanel.getMouseMotionListeners()[0].mouseMoved(hoverApo);
                hovered = hoveredField.getInt(mapPanel);
                assert hovered == MapSelectPanel.LANDMARK_MT_APO : "Hovered landmark must be Mt Apo (3), got: " + hovered;
                System.out.println("  ✓ Hover on Mt Apo (index 3) detected with pixel-perfect hit-test.");

                System.out.println("=== ALL 4-MAP INTEGRATION & STAGING TESTS PASSED! ===");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        System.exit(0);
    }
}
