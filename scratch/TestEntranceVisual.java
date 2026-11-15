import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import juanword.JuanWord;
import juanword.BattlePanel;
import juanword.DataManager;
import juanword.EnemyFactory;
import juanword.Player;
import javax.swing.SwingUtilities;

public class TestEntranceVisual {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P001", "Juan");
                game.setCurrentPlayer(juan);
                BattlePanel bp = new BattlePanel(game, DataManager.getInstance());
                bp.setSize(960, 540);
                bp.setupBattle(juan, "San Pedro Cathedral", 1, "Sakto");
                bp.doLayout();
                
                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                bp.paint(g2);
                g2.dispose();
                ImageIO.write(img, "png", new File("scratch/battle_render_check.png"));
                System.out.println("Battle screen rendered.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
