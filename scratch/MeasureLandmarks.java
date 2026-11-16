package scratch;

import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class MeasureLandmarks {
    public static void main(String[] args) {
        JuanWord app = new JuanWord();
        Player p = new Player(1, "Test");
        p.setUnlockedMap(4);
        app.setActivePlayer(p);
        MapSelectPanel msp = new MapSelectPanel(app);
        msp.setSize(960, 540);
        msp.refresh();
        for (int i = 0; i < msp.getLandmarks().size(); i++) {
            Landmark lm = msp.getLandmarks().get(i);
            System.out.println("Map " + (i+1) + " bounds: " + lm.getBounds());
        }
    }
}
