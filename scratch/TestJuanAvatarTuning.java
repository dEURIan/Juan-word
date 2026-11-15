import juanword.*;
import juanword.BattlePanel.HeroAnimState;
import juanword.BattlePanel.JuanAvatarPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class TestJuanAvatarTuning {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame();
                frame.setSize(960, 540);
                frame.setLayout(new BorderLayout());

                JuanWord game = new JuanWord();
                Player player = new Player("P001", "Juan");
                BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
                battle.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

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

                JuanAvatarPanel avatar = battle.getJuanAvatarPanel();

                // Select ABAGA
                battle.restoreTiles("A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;G:2:STANDARD;A:1:STANDARD;D:3:STANDARD;A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;W:4:STANDARD;E:1:STANDARD;N:2:HEAL;Y:3:STANDARD;O:1:STANDARD;L:2:STANDARD;A:1:STANDARD");
                Field tilesField = BattlePanel.class.getDeclaredField("tiles");
                tilesField.setAccessible(true);
                JButton[] tiles = (JButton[]) tilesField.get(battle);
                tiles[0].doClick();
                tiles[1].doClick();
                tiles[2].doClick();
                tiles[3].doClick();
                tiles[4].doClick();
                battle.repaint();

                // Render IDLE
                avatar.setManualStateForTesting(HeroAnimState.IDLE, 0);
                BufferedImage imgIdle = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g1 = imgIdle.createGraphics();
                battle.paint(g1);
                g1.dispose();
                ImageIO.write(imgIdle, "PNG", new File("scratch/tuned_juan_idle.png"));

                // Render ATTACK
                avatar.setManualStateForTesting(HeroAnimState.ATTACK, 42);
                BufferedImage imgAtt = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = imgAtt.createGraphics();
                battle.paint(g2);
                g2.dispose();
                ImageIO.write(imgAtt, "PNG", new File("scratch/tuned_juan_attack.png"));

                System.out.println("Rendered tuned screenshots successfully!");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
