import juanword.*;
import juanword.BattlePanel.HeroAnimState;
import juanword.BattlePanel.JuanAvatarPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class CaptureJuanAttackBattleScreenshots {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame();
                frame.setSize(960, 540);
                frame.setLayout(new BorderLayout());

                JuanWord game = new JuanWord();
                Player player = new Player("P001", "Juan");
                player.currentHealth = 85;
                player.maxHealth = 100;
                player.coins = 120;
                player.experience = 240;

                BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
                battle.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

                // Dismiss title card overlay
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

                JuanAvatarPanel avatar = battle.getJuanAvatarPanel();

                // 1. Capture Idle Stance
                avatar.setManualStateForTesting(HeroAnimState.IDLE, 0);
                BufferedImage imgIdle = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g1 = imgIdle.createGraphics();
                battle.paint(g1);
                g1.dispose();
                ImageIO.write(imgIdle, "PNG", new File("scratch/battle_juan_idle_stance.png"));
                ImageIO.write(imgIdle, "PNG", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_juan_idle_stance.png"));

                // 2. Capture Pre-Attack Wind-Up Stance
                avatar.setManualStateForTesting(HeroAnimState.PRE_ATTACK, -6);
                BufferedImage imgPre = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = imgPre.createGraphics();
                battle.paint(g2);
                g2.dispose();
                ImageIO.write(imgPre, "PNG", new File("scratch/battle_juan_preattack_stance.png"));
                ImageIO.write(imgPre, "PNG", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_juan_preattack_stance.png"));

                // 3. Capture Attack Strike Lunge Stance
                avatar.setManualStateForTesting(HeroAnimState.ATTACK, 42);
                BufferedImage imgAtt = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g3 = imgAtt.createGraphics();
                battle.paint(g3);
                g3.dispose();
                ImageIO.write(imgAtt, "PNG", new File("scratch/battle_juan_attack_stance.png"));
                ImageIO.write(imgAtt, "PNG", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_juan_attack_stance.png"));

                // 4. Create side-by-side progression carousel / composite
                BufferedImage collage = new BufferedImage(960, 540 * 3, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gCol = collage.createGraphics();
                gCol.drawImage(imgIdle, 0, 0, null);
                gCol.drawImage(imgPre, 0, 540, null);
                gCol.drawImage(imgAtt, 0, 1080, null);
                gCol.dispose();
                ImageIO.write(collage, "PNG", new File("scratch/battle_juan_attack_progression.png"));
                ImageIO.write(collage, "PNG", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_juan_attack_progression.png"));

                System.out.println("Captured all battle animation screenshots successfully!");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
