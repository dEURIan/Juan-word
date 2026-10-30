import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class CaptureBattlePreview {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame();
                frame.setSize(960, 540);
                frame.setLayout(new BorderLayout());

                JuanWord app = new JuanWord();
                BattlePanel panel = new BattlePanel(app, DataManager.getInstance());
                Player player = new Player("P001", "Juan");
                panel.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

                // Dismiss title card overlay for preview so we can see the full battle arena
                Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
                overlayField.setAccessible(true);
                JPanel overlay = (JPanel) overlayField.get(panel);
                if (overlay != null) overlay.setVisible(false);

                frame.add(panel, BorderLayout.CENTER);
                frame.pack();
                frame.setSize(960, 540);
                frame.validate();
                panel.setSize(960, 540);
                panel.validate();

                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                panel.paint(g2);
                g2.dispose();

                File out = new File("scratch/battle_preview.png");
                ImageIO.write(img, "png", out);
                System.out.println("Saved battle preview to " + out.getAbsolutePath());
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
