import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestEnemyVisuals {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                DataManager dm = DataManager.getInstance();
                Player player = new Player("Juan", "juan");
                player.currentHealth = 100;

                int[] stages = {1, 2, 3, 4};
                String[] stageNames = {"altarboy", "grandma", "sacristan_mayor", "alviola_boss"};

                for (int i = 0; i < stages.length; i++) {
                    int st = stages[i];
                    JFrame frame = new JFrame();
                    frame.setSize(1280, 720);
                    frame.setLayout(new BorderLayout());

                    BattlePanel bp = new BattlePanel(app, dm);
                    bp.setupBattle(player, "San Pedro Cathedral", st, "Sakto");
                    bp.dismissTitleCardForTesting();

                    frame.add(bp, BorderLayout.CENTER);
                    frame.pack();
                    frame.setSize(1280, 720);
                    frame.validate();
                    bp.setSize(1280, 720);
                    bp.validate();

                    BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = img.createGraphics();
                    bp.paint(g2);
                    g2.dispose();

                    File out = new File("scratch/stage_" + st + "_" + stageNames[i] + ".png");
                    ImageIO.write(img, "PNG", out);
                    System.out.println("Saved: " + out.getAbsolutePath());
                    frame.dispose();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
