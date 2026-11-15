package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import juanword.*;

public class TestMapExpandAndTileCentering {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting Map Expand & Tile Centering Verification ===");
        final CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        SwingUtilities.invokeLater(() -> {
            try {
                JuanWord app = new JuanWord();
                Player player = new Player("P001", "Juan");
                player.unlockedMapIndex = 1;
                app.setCurrentPlayer(player);
                DataManager dm = DataManager.getInstance();

                // -------------------------------------------------------------
                // TEST 1: MapSelectPanel Expand Transition
                // -------------------------------------------------------------
                MapSelectPanel mapPanel = new MapSelectPanel(app);
                mapPanel.setSize(960, 640);
                mapPanel.doLayout();

                assert !mapPanel.isExpanding() : "MapSelectPanel should not be expanding initially";

                // Snapshot Resting Map Screen
                BufferedImage imgResting = new BufferedImage(960, 640, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gRest = imgResting.createGraphics();
                mapPanel.paint(gRest);
                gRest.dispose();
                ImageIO.write(imgResting, "PNG", new File("scratch/map_resting_screen.png"));
                System.out.println("✔ Resting map screen captured.");

                // Simulate expansion progress at 50%
                mapPanel.setExpandProgressForTesting(MapSelectPanel.LANDMARK_BLUE_BUILDING, 0.50f);
                assert mapPanel.isExpanding() : "MapSelectPanel should report isExpanding() = true";
                assert mapPanel.getExpandingLandmark() == MapSelectPanel.LANDMARK_BLUE_BUILDING : "Expanding landmark must be Blue Building";

                BufferedImage imgMidExpand = new BufferedImage(960, 640, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gMid = imgMidExpand.createGraphics();
                mapPanel.paint(gMid);
                gMid.dispose();
                ImageIO.write(imgMidExpand, "PNG", new File("scratch/map_expanding_50pct.png"));
                System.out.println("✔ Mid-expansion (50%) map screen captured.");

                // Simulate expansion progress at 95%
                mapPanel.setExpandProgressForTesting(MapSelectPanel.LANDMARK_BLUE_BUILDING, 0.95f);
                BufferedImage imgNearFull = new BufferedImage(960, 640, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gNearFull = imgNearFull.createGraphics();
                mapPanel.paint(gNearFull);
                gNearFull.dispose();
                ImageIO.write(imgNearFull, "PNG", new File("scratch/map_expanding_95pct.png"));
                System.out.println("✔ Near-full expansion (95%) map screen captured.");

                // -------------------------------------------------------------
                // TEST 2: BattlePanel Background & Tile Grid Centering
                // -------------------------------------------------------------
                BattlePanel battle = new BattlePanel(app, dm);
                battle.setSize(960, 640);
                battle.setupBattle(player, "Blue Building", 1, "Sayon");
                validateHierarchy(battle);

                // Snapshot Battle Screen
                BufferedImage imgBattle = new BufferedImage(960, 640, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBattle = imgBattle.createGraphics();
                battle.paint(gBattle);
                gBattle.dispose();
                ImageIO.write(imgBattle, "PNG", new File("scratch/battle_centered_tiles_and_bg.png"));
                System.out.println("✔ Battle screen with new background composition and tiles captured.");

                // Find the gridPanel inside battle
                JPanel gridPanel = findGridPanel(battle);
                assert gridPanel != null : "gridPanel must exist in BattlePanel";

                Point pOnBattle = SwingUtilities.convertPoint(gridPanel, 0, 0, battle);
                int gridCenterX = pOnBattle.x + gridPanel.getWidth() / 2;
                System.out.println("Grid bounds on battle screen: x=" + pOnBattle.x + ", w=" + gridPanel.getWidth() + ", center=" + gridCenterX);
                assert Math.abs(gridCenterX - 480) <= 2 : "Grid must be centered on the 960px screen (expected ~480, got " + gridCenterX + ")";
                System.out.println("✔ Letter tile grid is MATHEMATICALLY CENTERED at x=" + gridCenterX);

                // Check letter tile buttons are square
                Component[] tileComps = gridPanel.getComponents();
                assert tileComps.length == 16 : "Must have 16 letter tiles";
                for (int i = 0; i < tileComps.length; i++) {
                    Dimension d = tileComps[i].getPreferredSize();
                    assert d.width == d.height : "Tile " + i + " must be square, got: " + d;
                }
                System.out.println("✔ All 16 Letter tiles have square dimensions: " + tileComps[0].getPreferredSize());

                System.out.println("=== All Map Expand & Tile Centering Tests Passed! ===");
            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        boolean done = latch.await(10, TimeUnit.SECONDS);
        if (!done) {
            throw new RuntimeException("Test timed out!");
        }
        if (error[0] != null) {
            if (error[0] instanceof Exception) throw (Exception) error[0];
            if (error[0] instanceof Error) throw (Error) error[0];
            throw new RuntimeException(error[0]);
        }
    }

    private static void validateHierarchy(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                validateHierarchy((Container) child);
            }
        }
    }

    private static JPanel findGridPanel(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                if (p.getLayout() instanceof GridLayout) {
                    GridLayout gl = (GridLayout) p.getLayout();
                    if (gl.getRows() == 4 && gl.getColumns() == 4) {
                        return p;
                    }
                }
            }
            if (c instanceof Container) {
                JPanel found = findGridPanel((Container) c);
                if (found != null) return found;
            }
        }
        return null;
    }
}
