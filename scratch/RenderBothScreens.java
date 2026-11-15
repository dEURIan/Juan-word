import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import juanword.JuanWord;
import juanword.MapSelectPanel;
import juanword.Player;
import java.lang.reflect.Field;
import javax.swing.SwingUtilities;

public class RenderBothScreens {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P001", "Juan");
                juan.unlockedMapIndex = 1;
                game.setCurrentPlayer(juan);
                game.setSize(960, 540);
                
                Field mpField = JuanWord.class.getDeclaredField("mapSelectPanel");
                mpField.setAccessible(true);
                MapSelectPanel mapPanel = (MapSelectPanel) mpField.get(game);
                mapPanel.setSize(960, 540);
                mapPanel.doLayout();
                
                Field animField = MapSelectPanel.class.getDeclaredField("animProgress");
                animField.setAccessible(true);
                animField.setFloat(mapPanel, 1.0f);
                
                // Let's render the base resting screen
                BufferedImage base = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBase = base.createGraphics();
                mapPanel.paint(gBase);
                gBase.dispose();
                ImageIO.write(base, "png", new File("scratch/current_base_screen.png"));
                System.out.println("Base screen rendered.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
