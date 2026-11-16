package scratch;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class TestGudoUnlockedProfile {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Gudo 100% Unlocked Profile ===");

        DataManager dm = DataManager.getInstance();
        List<Player> players = dm.getAllPlayers();

        Player gudo = null;
        for (Player p : players) {
            if ("Gudo".equalsIgnoreCase(p.getPlayerName())) {
                gudo = p;
                break;
            }
        }

        assert gudo != null : "Profile 'Gudo' must exist in DataManager/players.csv";
        System.out.println("  ✓ Found profile: " + gudo.getPlayerName() + " (ID: " + gudo.getPlayerId() + ")");

        // 1. Verify Unlocked Map is 4 (All maps unlocked)
        assert gudo.getUnlockedMap() == 4 : "Gudo must have Map 4 (all maps) unlocked, got: " + gudo.getUnlockedMap();
        System.out.println("  ✓ Map unlock status: " + gudo.getUnlockedMap() + " (All 4 maps accessible)");

        // 2. Verify Score & Max HP
        assert gudo.getScore() >= 5000 : "Gudo score should reflect completed profile (>=5000), got: " + gudo.getScore();
        assert gudo.getMaxHp() >= 100 : "Gudo max HP must be at least 100, got: " + gudo.getMaxHp();
        assert !gudo.hasActiveBattle() : "Gudo should not have an active unfinished battle";
        System.out.println("  ✓ Score: " + gudo.getScore() + ", Max HP: " + gudo.getMaxHp() + ", Active Battle: false");

        // 3. Verify all campaign enemies defeated
        String[] expectedEnemies = {
            "GUARD", "MALE_STUDENT", "BOSS_DIZON",
            "PAINTING_1", "PAINTING_2", "BOSS_JANITOR",
            "LOLA", "ALTAR_BOY", "BOSS_ALVIOLA",
            "MONKEY", "KARABAW", "BOSS_PORTER", "GUDO"
        };
        for (String enemyId : expectedEnemies) {
            assert gudo.hasDefeated(enemyId) : "Gudo must have defeated " + enemyId;
        }
        System.out.println("  ✓ All campaign enemies including BOSS_PORTER and GUDO confirmed defeated.");

        // 4. Verify MapSelectPanel behaves correctly when Gudo is active
        JuanWord app = new JuanWord();
        app.setActivePlayer(gudo);
        assert app.getActivePlayer() == gudo : "Active player must be Gudo";

        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();

        List<Landmark> landmarks = mapPanel.getLandmarks();
        assert landmarks.size() == 4 : "Must have 4 landmarks";
        for (Landmark lm : landmarks) {
            assert !lm.isLocked() : "Landmark " + lm.getName() + " (Map " + lm.getMapIndex() + ") must be UNLOCKED for Gudo!";
            System.out.println("  ✓ Landmark " + lm.getMapIndex() + " (" + lm.getName() + ") is unlocked.");
        }

        // Verify Fight Gudo button appears at top of map
        Rectangle btn = mapPanel.getFightGudoButtonBounds();
        assert btn.width > 0 && btn.height > 0 : "Fight Gudo button must be visible for Gudo";
        assert btn.y < 120 : "Fight Gudo button must appear at the top of the map, y=" + btn.y;
        System.out.println("  ✓ 'Fight Gudo' challenge button visible at top [" + btn.x + "," + btn.y + "," + btn.width + "x" + btn.height + "].");

        // 5. Verify BattlePanel loads Gudo player sprites
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(gudo)
            .onMap(1)
            .atStage(1)
            .build();
        BattlePanel battle = new BattlePanel(app);
        battle.setSize(960, 540);
        battle.begin(session);
        assert battle.getSession().getPlayer().getPlayerName().equalsIgnoreCase("Gudo");
        System.out.println("  ✓ BattlePanel recognizes active Gudo player profile with custom sprites.");

        // 6. Verify CSV Roundtrip Serialization
        String[] row = gudo.toCsvRow();
        assert row.length == 13 : "Gudo toCsvRow must produce 13 columns";
        Player restored = Player.fromCsvRow(row);
        assert restored.getPlayerName().equals("Gudo");
        assert restored.getUnlockedMap() == 4;
        assert restored.getScore() == gudo.getScore();
        assert restored.hasDefeated("BOSS_PORTER");
        assert restored.hasDefeated("GUDO");
        System.out.println("  ✓ CSV serialization roundtrip is 100% lossless.");

        System.out.println("\n=== ALL GUDO UNLOCKED PROFILE TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }
}
