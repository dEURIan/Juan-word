package scratch;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;

public class RenderAllEnemiesInBattle {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        Player player = new Player(1, "Juan");
        app.setActivePlayer(player);

        int mapCount = 4;
        int stagesPerMap = 3;

        for (int map = 1; map <= mapCount; map++) {
            for (int stage = 1; stage <= stagesPerMap; stage++) {
                BattleSession session = new BattleSessionBuilder()
                    .withPlayer(player)
                    .onMap(map)
                    .atStage(stage)
                    .build();

                BattlePanel battlePanel = new BattlePanel(app);
                battlePanel.setSize(960, 540);
                battlePanel.begin(session);
                battlePanel.getEnemyAnimator().completeEntrance();
                battlePanel.doLayout();

                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                battlePanel.paint(g2);
                g2.dispose();

                String filename = String.format("scratch/battle_m%d_s%d_%s.png", map, stage, session.getEnemy().getEnemyId().toLowerCase());
                ImageIO.write(img, "PNG", new File(filename));
                System.out.println("Rendered " + filename + " -> " + session.getEnemy().getName());
            }
        }
        System.exit(0);
    }
}
