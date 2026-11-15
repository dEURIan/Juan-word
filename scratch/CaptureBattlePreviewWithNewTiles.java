import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class CaptureBattlePreviewWithNewTiles {
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

                // Dismiss title card overlay for preview
                Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                overlayField.setAccessible(true);
                JPanel overlay = (JPanel) overlayField.get(battlePanel);
                if (overlay != null) overlay.setVisible(false);

                frame.add(battlePanel, BorderLayout.CENTER);
                frame.pack();
                frame.setSize(960, 540);
                frame.validate();
                battlePanel.setSize(960, 540);
                battlePanel.validate();

                // Set up letters: "SAYON" on row 1
                battlePanel.restoreTiles("S:2:STANDARD;A:1:STANDARD;Y:3:STANDARD;O:1:STANDARD;N:2:GOLD;D:3:STANDARD;A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;W:4:STANDARD;E:1:STANDARD;N:2:HEAL;Y:3:STANDARD;O:1:STANDARD;L:2:STANDARD;A:1:STANDARD");

                Field tilesField = BattlePanel.class.getDeclaredField("tiles");
                tilesField.setAccessible(true);
                JButton[] tiles = (JButton[]) tilesField.get(battlePanel);

                // Click first 5 tiles to pop "SAYON" onto the word rack
                tiles[0].doClick();
                tiles[1].doClick();
                tiles[2].doClick();
                tiles[3].doClick();
                tiles[4].doClick();

                frame.validate();
                battlePanel.validate();
                battlePanel.repaint();

                // Render battle screen to image
                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                battlePanel.paint(g2);
                g2.dispose();

                File out = new File("scratch/battle_with_new_alphabet_tiles.png");
                ImageIO.write(img, "png", out);
                ImageIO.write(img, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/battle_with_new_alphabet_tiles.png"));
                System.out.println("✓ Successfully captured battle screen with new alphabet tiles to: " + out.getAbsolutePath());

                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        System.exit(0);
    }
}
