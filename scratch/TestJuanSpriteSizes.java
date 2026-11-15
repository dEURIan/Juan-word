package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.swing.*;
import juanword.BattlePanel;
import juanword.DataManager;
import juanword.JuanWord;
import juanword.Player;

public class TestJuanSpriteSizes {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                int[] testHeights = {85, 95, 105, 115};
                BufferedImage collage = new BufferedImage(960, 540 * testHeights.length, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gCollage = collage.createGraphics();

                for (int i = 0; i < testHeights.length; i++) {
                    int h = testHeights[i];
                    int w = (int) Math.round(h * (300.0 / 500.0));

                    JuanWord game = new JuanWord();
                    Player player = new Player("P001", "Juan");
                    player.currentHealth = 85;
                    player.maxHealth = 100;
                    player.coins = 120;
                    player.experience = 240;

                    BattlePanel battlePanel = new BattlePanel(game, DataManager.getInstance());
                    battlePanel.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

                    // Hide overlay
                    Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                    overlayField.setAccessible(true);
                    JPanel overlay = (JPanel) overlayField.get(battlePanel);
                    if (overlay != null) overlay.setVisible(false);

                    JFrame frame = new JFrame();
                    frame.setSize(960, 540);
                    frame.add(battlePanel);
                    frame.pack();
                    frame.setSize(960, 540);
                    frame.validate();
                    battlePanel.setSize(960, 540);
                    battlePanel.validate();

                    BufferedImage frameImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = frameImg.createGraphics();
                    battlePanel.paint(g);
                    g.dispose();

                    gCollage.drawImage(frameImg, 0, i * 540, null);
                    gCollage.setColor(Color.YELLOW);
                    gCollage.setFont(new Font("Arial", Font.BOLD, 18));
                    gCollage.drawString("Testing Height: " + h + "px, Width: " + w + "px", 30, i * 540 + 40);

                    frame.dispose();
                }

                gCollage.dispose();
                File out = new File("scratch/juan_size_comparison.png");
                ImageIO.write(collage, "PNG", out);
                System.out.println("Rendered comparison to " + out.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
