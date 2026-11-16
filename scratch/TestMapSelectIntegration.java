import java.awt.Point;
import java.awt.Rectangle;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class TestMapSelectIntegration {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing 4-Map Architecture & Map Select Integration ===");

        // 1. Verify Player Progression Model
        Player juan = new Player(1, "Juan");
        assert juan.getUnlockedMap() == 1 : "Juan must only have Map 1 unlocked initially";

        // 2. Verify Predefined Difficulties
        assert DifficultySetting.forMap(1) == DifficultySetting.SAYON : "Map 1 must be Sayon";
        assert DifficultySetting.forMap(2) == DifficultySetting.SAKTO : "Map 2 must be Sakto";
        assert DifficultySetting.forMap(3) == DifficultySetting.LISOD : "Map 3 must be Lisod";
        assert DifficultySetting.forMap(4) == DifficultySetting.EXTREME : "Map 4 must be Extreme";
        System.out.println("  ✓ Predefined difficulties verified: Sayon, Sakto, Lisod, Extreme.");

        // 3. Verify Map Progression Unlock Logic
        Player testProg = new Player(99, "ProgTester");
        assert testProg.getUnlockedMap() == 1;

        testProg.unlockMap(2);
        assert testProg.getUnlockedMap() == 2;

        testProg.unlockMap(3);
        assert testProg.getUnlockedMap() == 3;

        testProg.unlockMap(4);
        assert testProg.getUnlockedMap() == 4;

        testProg.unlockMap(5);
        assert testProg.getUnlockedMap() == 4;
        System.out.println("  ✓ Map progression unlocks step-by-step up to Mount Apo.");

        // 4. Verify Landmark Model and Hit-Testing
        Landmark lm = new Landmark(1, "Ateneo Blue Building", "Blue Building.png", new Rectangle(40, 120, 200, 260));
        assert lm.contains(new Point(100, 150)) : "Point (100, 150) should be inside Landmark bounds";
        assert !lm.contains(new Point(10, 10)) : "Point (10, 10) should be outside Landmark bounds";

        lm.updateScale(true); // hovered
        assert lm.getScale() > 1.0f : "Hovered landmark should scale above 1.0";

        lm.setLocked(true);
        assert lm.isLocked() : "Landmark should be locked";

        // 5. Verify Mount Apo (Map 4) Hit-Testing and Summit Bounds
        Landmark apo = new Landmark(4, "Mount Apo", "mtapo.png", new Rectangle(0, 0, 960, 540));
        assert apo.getMapIndex() == 4 : "Mount Apo must be mapIndex 4";
        assert apo.contains(new Point(480, 160)) : "Summit Point (480, 160) should be inside Mount Apo";
        assert !apo.contains(new Point(100, 20)) : "Sky Point (100, 20) should be outside Mount Apo (y < 0.08 * h)";
        assert !apo.contains(new Point(100, 480)) : "Ground Point (100, 480) should be outside Mount Apo (y > 0.72 * h)";

        apo.updateScale(true);
        assert apo.getScale() > 1.0f : "Hovered Mount Apo should scale above 1.0";

        // 6. Verify MapSelectPanel Hit-Testing Precedence (Foreground Landmarks over Mount Apo)
        JuanWord app = new JuanWord();
        app.setActivePlayer(juan);
        MapSelectPanel panel = new MapSelectPanel(app);
        panel.setSize(960, 540);
        panel.refresh();

        // Mouse over Blue Building (e.g. 100, 350): Blue Building takes precedence over Mount Apo
        java.awt.event.MouseEvent meBB = new java.awt.event.MouseEvent(
            panel, java.awt.event.MouseEvent.MOUSE_MOVED,
            System.currentTimeMillis(), 0, 100, 350, 0, false
        );
        for (java.awt.event.MouseMotionListener mml : panel.getMouseMotionListeners()) {
            mml.mouseMoved(meBB);
        }
        assert panel.getCursor().getType() == java.awt.Cursor.HAND_CURSOR : "Cursor over landmark should be HAND_CURSOR";

        // Mouse over Mount Apo summit (480, 160) where no building exists: Mount Apo hovered
        java.awt.event.MouseEvent meApo = new java.awt.event.MouseEvent(
            panel, java.awt.event.MouseEvent.MOUSE_MOVED,
            System.currentTimeMillis(), 0, 480, 160, 0, false
        );
        for (java.awt.event.MouseMotionListener mml : panel.getMouseMotionListeners()) {
            mml.mouseMoved(meApo);
        }
        assert panel.getCursor().getType() == java.awt.Cursor.HAND_CURSOR : "Cursor over Mount Apo summit should be HAND_CURSOR";

        System.out.println("  ✓ Landmark hit-testing, Mount Apo summit, and hover scaling verified.");
        System.out.println("=== All Map Select Integration Tests Passed! ===");
        System.exit(0);
    }
}
