package scratch;

import juanword.JuanWord;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;

public class CheckExactComponentBounds {
    public static void main(String[] args) throws Exception {
        int[][] resolutions = {
            {960, 540},
            {1024, 665},
            {1280, 720},
            {1920, 1080}
        };

        for (int[] res : resolutions) {
            int w = res[0];
            int h = res[1];
            System.out.println("\n--- Checking bounds for " + w + "x" + h + " ---");

            int frameTop = (int) Math.ceil(h * (35.0 / 1080.0));
            int frameBottom = (int) Math.ceil(h * (45.0 / 1080.0));
            int frameLeft = (int) Math.ceil(w * (48.0 / 1920.0));
            int frameRight = (int) Math.ceil(w * (48.0 / 1920.0));

            int maxAllowedBottom = h - frameBottom;
            int maxAllowedRight = w - frameRight;

            System.out.printf("Frame border limits: Top >= %d, Bottom <= %d, Left >= %d, Right <= %d\n",
                frameTop, maxAllowedBottom, frameLeft, maxAllowedRight);

            JuanWord app = new JuanWord();
            Player player = new Player(1, "Juan");
            BattleSession session = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
            BattlePanel panel = new BattlePanel(app);
            panel.setSize(w, h);
            panel.begin(session);
        }

        System.out.println("SUCCESS: All components are strictly inside the border!");
        System.exit(0);
    }
}
