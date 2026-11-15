package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.swing.*;
import juanword.*;

public class RenderFullBattleWithNewJuan {
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

                BattlePanel battlePanel = new BattlePanel(game, DataManager.getInstance());
                battlePanel.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

                // Dismiss title card overlay
                Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                overlayField.setAccessible(true);
                JPanel overlay = (JPanel) overlayField.get(battlePanel);
                if (overlay != null) overlay.setVisible(false);

                // Replace juanAvatarPanel with prototype panel
                BufferedImage idle = ImageIO.read(new File("src/juanword/maincharacteridle.png"));
                BufferedImage pre = ImageIO.read(new File("src/juanword/maincharapreattack.png"));
                BufferedImage att = ImageIO.read(new File("src/juanword/maincharattack.png"));

                PrototypeJuanAnimation.TestJuanAvatarPanel newAvatar =
                    new PrototypeJuanAnimation.TestJuanAvatarPanel(idle, pre, att);

                Field juanAvatarField = BattlePanel.class.getDeclaredField("juanAvatarPanel");
                juanAvatarField.setAccessible(true);
                JPanel oldAvatar = (JPanel) juanAvatarField.get(battlePanel);
                Container juanSide = oldAvatar.getParent();
                juanSide.remove(oldAvatar);
                juanSide.add(newAvatar, BorderLayout.CENTER);
                juanAvatarField.set(battlePanel, newAvatar);

                frame.add(battlePanel, BorderLayout.CENTER);
                frame.pack();
                frame.setSize(960, 540);
                frame.validate();
                battlePanel.setSize(960, 540);
                battlePanel.validate();

                // 1. Render in IDLE
                newAvatar.setManualState(PrototypeJuanAnimation.HeroAnimState.IDLE, 0);
                BufferedImage idleImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g1 = idleImg.createGraphics();
                battlePanel.paint(g1);
                g1.dispose();
                ImageIO.write(idleImg, "PNG", new File("scratch/battle_juan_idle.png"));

                // 2. Render in PRE_ATTACK
                newAvatar.setManualState(PrototypeJuanAnimation.HeroAnimState.PRE_ATTACK, -6);
                BufferedImage preImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = preImg.createGraphics();
                battlePanel.paint(g2);
                g2.dispose();
                ImageIO.write(preImg, "PNG", new File("scratch/battle_juan_preattack.png"));

                // 3. Render in ATTACK (Apex lunge)
                newAvatar.setManualState(PrototypeJuanAnimation.HeroAnimState.ATTACK, 42);
                BufferedImage attImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g3 = attImg.createGraphics();
                battlePanel.paint(g3);
                g3.dispose();
                ImageIO.write(attImg, "PNG", new File("scratch/battle_juan_attack.png"));

                System.out.println("Rendered full battle states successfully!");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
