import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestSpriteHitAndTransitions {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                DataManager dm = DataManager.getInstance();
                Player player = new Player("Juan", "juan");

                // Test Stage 1 Altar Boy Hurt
                BattlePanel bp1 = new BattlePanel(app, dm);
                bp1.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");
                bp1.dismissTitleCardForTesting();
                bp1.setSize(1280, 720);
                bp1.doLayout();

                // Reflectively trigger isHurt on dizonBox
                java.lang.reflect.Field boxField = BattlePanel.class.getDeclaredField("dizonBox");
                boxField.setAccessible(true);
                JPanel dizonBox = (JPanel) boxField.get(bp1);
                java.lang.reflect.Field hurtField = dizonBox.getClass().getSuperclass().getDeclaredField("isHurt");
                hurtField.setAccessible(true);
                hurtField.set(dizonBox, true);

                BufferedImage imgHurt1 = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gH1 = imgHurt1.createGraphics();
                bp1.paint(gH1);
                gH1.dispose();
                ImageIO.write(imgHurt1, "PNG", new File("scratch/altarboy_hurt_frame.png"));
                System.out.println("Saved altarboy_hurt_frame.png");

                // Test Stage 2 Grandma Hurt
                BattlePanel bp2 = new BattlePanel(app, dm);
                bp2.setupBattle(player, "San Pedro Cathedral", 2, "Sakto");
                bp2.dismissTitleCardForTesting();
                bp2.setSize(1280, 720);
                bp2.doLayout();

                JPanel dizonBox2 = (JPanel) boxField.get(bp2);
                hurtField.set(dizonBox2, true);

                BufferedImage imgHurt2 = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gH2 = imgHurt2.createGraphics();
                bp2.paint(gH2);
                gH2.dispose();
                ImageIO.write(imgHurt2, "PNG", new File("scratch/grandma_hurt_frame.png"));
                System.out.println("Saved grandma_hurt_frame.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
