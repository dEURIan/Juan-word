import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class TestExpandAndBattleComposite {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P001", "Juan");
                juan.unlockedMapIndex = 1;
                game.setCurrentPlayer(juan);

                // --- 1. Test Map Selection Screen with Expand Frame ---
                MapSelectPanel mapPanel = new MapSelectPanel(game);
                mapPanel.setSize(960, 540);
                mapPanel.doLayout();

                Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
                animField.setAccessible(true);
                animField.setFloat(mapPanel, 1.0f);

                // Render Resting Map Screen
                BufferedImage mapRest = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gMap = mapRest.createGraphics();
                mapPanel.paint(gMap);
                gMap.dispose();
                ImageIO.write(mapRest, "PNG", new File("scratch/test_map_resting.png"));
                System.out.println("Rendered resting map selection screen.");

                // --- 2. Test Battle Screen Layout ---
                BattlePanel bp = new BattlePanel(game, DataManager.getInstance());
                bp.setupBattle(juan, "Blue Building", 1, "Sayon");

                Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                overlayField.setAccessible(true);
                JPanel overlay = (JPanel) overlayField.get(bp);
                if (overlay != null) overlay.setVisible(false);

                JFrame frame = new JFrame();
                frame.setSize(960, 540);
                frame.setContentPane(bp);
                frame.pack();
                frame.setSize(960, 540);
                frame.validate();
                bp.setSize(960, 540);
                bp.validate();

                // Select ABAGA
                bp.restoreTiles("A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;G:2:STANDARD;A:1:STANDARD;D:3:STANDARD;A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;W:4:STANDARD;E:1:STANDARD;N:2:HEAL;Y:3:STANDARD;O:1:STANDARD;L:2:STANDARD;A:1:STANDARD");
                Field tilesField = BattlePanel.class.getDeclaredField("tiles");
                tilesField.setAccessible(true);
                JButton[] tiles = (JButton[]) tilesField.get(bp);
                tiles[0].doClick();
                tiles[1].doClick();
                tiles[2].doClick();
                tiles[3].doClick();
                tiles[4].doClick();
                bp.repaint();

                BufferedImage battleImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gB = battleImg.createGraphics();
                bp.paint(gB);
                gB.dispose();
                ImageIO.write(battleImg, "PNG", new File("scratch/test_battle_with_current_layout.png"));

                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
