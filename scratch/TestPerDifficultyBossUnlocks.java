import juanword.data.DataManager;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.model.DifficultySetting;
import juanword.model.Player;

public class TestPerDifficultyBossUnlocks {

    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: MAP PROGRESSION & BOSS UNLOCKS ===");

        testDefaultUnlocks();
        testUnlockBossOnSpecificMap();
        testUnlockPersistenceAcrossSaveAndReload();
        testFourDifficulties();

        System.out.println("\n=== ALL MAP & BOSS UNLOCK TESTS PASSED! ===");
        System.exit(0);
    }

    private static void testDefaultUnlocks() {
        System.out.println("\n--- 1. Testing Default Unlocks (Fresh Player) ---");
        Player p = new Player(999, "TestFresh");

        assert p.getUnlockedMap() == 1 : "Fresh player must start at Map 1";
        assert !p.hasDefeated("BOSS_DIZON") : "Boss Dizon should not be defeated yet";
        assert !p.hasDefeated("BOSS_JANITOR") : "Boss Janitor should not be defeated yet";

        System.out.println("  ✓ Default state: Map 1 unlocked, all bosses undefeated.");
    }

    private static void testUnlockBossOnSpecificMap() {
        System.out.println("\n--- 2. Testing Unlocking Subsequent Maps ---");
        Player p = new Player(999, "TestUnlock");

        // Defeating Map 1 Boss unlocks Map 2
        p.recordDefeat("BOSS_DIZON");
        p.unlockMap(2);
        assert p.getUnlockedMap() == 2 : "Defeating Map 1 Boss should unlock Map 2";
        assert p.hasDefeated("BOSS_DIZON") : "Boss Dizon must be recorded as defeated";

        // Defeating Map 2 Boss unlocks Map 3
        p.recordDefeat("BOSS_JANITOR");
        p.unlockMap(3);
        assert p.getUnlockedMap() == 3 : "Defeating Map 2 Boss should unlock Map 3";

        // Defeating Map 3 Boss unlocks Map 4
        p.recordDefeat("BOSS_ALVIOLA");
        p.unlockMap(4);
        assert p.getUnlockedMap() == 4 : "Defeating Map 3 Boss should unlock Map 4";

        // Cannot unlock beyond Map 4
        p.unlockMap(5);
        assert p.getUnlockedMap() == 4 : "Cannot unlock map beyond 4";

        System.out.println("  ✓ Map unlock progression successfully tested up to Map 4.");
    }

    private static void testUnlockPersistenceAcrossSaveAndReload() {
        System.out.println("\n--- 3. Testing Unlock Persistence Across CSV Serialization ---");
        Player p = new Player(888, "TestPersist");
        p.unlockMap(3);
        p.recordDefeat("BOSS_DIZON");
        p.recordDefeat("BOSS_JANITOR");

        String[] row = p.toCsvRow();
        Player loaded = Player.fromCsvRow(row);

        assert loaded.getUnlockedMap() == 3 : "Loaded player should have Map 3 unlocked";
        assert loaded.hasDefeated("BOSS_DIZON") : "Loaded player should remember defeating Boss Dizon";
        assert loaded.hasDefeated("BOSS_JANITOR") : "Loaded player should remember defeating Boss Janitor";

        System.out.println("  ✓ Player progression correctly persisted and restored.");
    }

    private static void testFourDifficulties() {
        System.out.println("\n--- 4. Testing Difficulty Mapping Across 4 Maps ---");
        assert DifficultySetting.forMap(1) == DifficultySetting.SAYON : "Map 1 must be SAYON";
        assert DifficultySetting.forMap(2) == DifficultySetting.SAKTO : "Map 2 must be SAKTO";
        assert DifficultySetting.forMap(3) == DifficultySetting.LISOD : "Map 3 must be LISOD";
        assert DifficultySetting.forMap(4) == DifficultySetting.EXTREME : "Map 4 must be EXTREME";

        assert !DifficultySetting.SAYON.isTimed() : "Sayon is untimed";
        assert !DifficultySetting.SAKTO.isTimed() : "Sakto is untimed";
        assert DifficultySetting.LISOD.isTimed() : "Lisod is timed (25s)";
        assert DifficultySetting.EXTREME.isTimed() : "Extreme is timed (20s)";

        System.out.println("  ✓ All 4 difficulties match campaign specifications.");
    }
}
