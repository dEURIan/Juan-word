import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class TestBottomPanelLayout {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame();
                frame.setSize(960, 540);
                frame.setLayout(new BorderLayout());

                JuanWord game = new JuanWord();
                Player player = new Player("P001", "Juan");
                BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
                battle.setupBattle(player, "Blue Building", 1, "Sayon");

                // Hide overlay
                Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                overlayField.setAccessible(true);
                JPanel overlay = (JPanel) overlayField.get(battle);
                if (overlay != null) overlay.setVisible(false);

                frame.add(battle, BorderLayout.CENTER);
                frame.pack();
                frame.setSize(960, 540);
                frame.validate();
                battle.setSize(960, 540);
                battle.validate();

                // Restore letters: A B A G A
                battle.restoreTiles("N:1:STANDARD;L:2:STANDARD;N:1:STANDARD;N:1:STANDARD;R:1:STANDARD;L:2:STANDARD;A:1:STANDARD;T:1:STANDARD;D:1:STANDARD;L:2:STANDARD;K:1:STANDARD;L:2:STANDARD;A:1:STANDARD;Y:1:STANDARD;T:1:STANDARD;N:1:STANDARD");

                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                battle.paint(g);
                g.dispose();

                ImageIO.write(img, "PNG", new File("scratch/current_battle_before_fix.png"));
                System.out.println("Captured current battle screen before fix.");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
