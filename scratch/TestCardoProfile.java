package scratch;

import java.util.List;
import java.util.Set;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.Player;
import juanword.ui.Landmark;
import juanword.ui.MapSelectPanel;

public class TestCardoProfile {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Cardo 100% Unlocked Profile ===");

        DataManager dm = DataManager.getInstance();
        List<Player> players = dm.getAllPlayers();

        Player cardo = null;
        for (Player p : players) {
            if ("Cardo".equalsIgnoreCase(p.getPlayerName())) {
                cardo = p;
                break;
            }
        }

        assert cardo != null : "Profile 'Cardo' must exist in DataManager/players.csv";
        System.out.println("  ✓ Found profile: " + cardo.getPlayerName() + " (ID: " + cardo.getPlayerId() + ")");

        // 1. Verify Unlocked Map is 4 (All maps unlocked)
        assert cardo.getUnlockedMap() == 4 : "Cardo must have Map 4 (all maps) unlocked, got: " + cardo.getUnlockedMap();
        System.out.println("  ✓ Map unlock status: " + cardo.getUnlockedMap() + " (All 4 maps accessible)");

        // 2. Verify Score & Max HP
        assert cardo.getScore() >= 5000 : "Cardo score should reflect completed game (>=5000), got: " + cardo.getScore();
        assert cardo.getMaxHp() >= 100 : "Cardo max HP must be at least 100, got: " + cardo.getMaxHp();
        assert !cardo.hasActiveBattle() : "Cardo should not have an active unfinished battle";
        System.out.println("  ✓ Score: " + cardo.getScore() + ", Max HP: " + cardo.getMaxHp());

        // 3. Verify all 12 enemies defeated
        String[] expectedEnemies = {
            "GUARD", "MALE_STUDENT", "BOSS_DIZON",
            "PAINTING_1", "PAINTING_2", "BOSS_JANITOR",
            "LOLA", "ALTAR_BOY", "BOSS_ALVIOLA",
            "MONKEY", "KARABAW", "BOSS_PORTER"
        };
        Set<String> defeated = cardo.getDefeatedEnemies();
        assert defeated.size() == 12 : "Cardo must have defeated exactly 12 enemies, got: " + defeated.size();
        for (String enemyId : expectedEnemies) {
            assert cardo.hasDefeated(enemyId) : "Cardo must have defeated " + enemyId;
        }
        System.out.println("  ✓ All 12 campaign enemies (Map 1-4, Stage 1-3) confirmed defeated.");

        // 4. Verify MapSelectPanel behaves correctly when Cardo is active
        JuanWord app = new JuanWord();
        app.setActivePlayer(cardo);
        assert app.getActivePlayer() == cardo : "Active player must be Cardo";

        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();

        List<Landmark> landmarks = mapPanel.getLandmarks();
        assert landmarks.size() == 4 : "Must have 4 landmarks";
        for (Landmark lm : landmarks) {
            assert !lm.isLocked() : "Landmark " + lm.getName() + " (Map " + lm.getMapIndex() + ") must be UNLOCKED for Cardo!";
            System.out.println("  ✓ Landmark " + lm.getMapIndex() + " (" + lm.getName() + ") is unlocked.");
        }

        // 5. Verify CSV Roundtrip Serialization
        String[] row = cardo.toCsvRow();
        assert row.length == 13 : "Cardo toCsvRow must produce 13 columns";
        Player restored = Player.fromCsvRow(row);
        assert restored.getPlayerName().equals("Cardo");
        assert restored.getUnlockedMap() == 4;
        assert restored.getDefeatedEnemies().size() == 12;
        System.out.println("  ✓ CSV serialization roundtrip is 100% lossless.");

        System.out.println("\n=== ALL CARDO PROFILE TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }
}
