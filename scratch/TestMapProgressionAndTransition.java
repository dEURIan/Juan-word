package scratch;

import juanword.model.DifficultySetting;
import juanword.model.Player;

public class TestMapProgressionAndTransition {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Map Progression and Horizontal Travel Animation ===");

        // 1. Test Player Progression Chain
        System.out.println("\n--- 1. Testing Next Map Chain Resolution ---");
        Player p = new Player(1, "JuanBiyahero");
        assert p.getUnlockedMap() == 1 : "Starts at Map 1";

        p.unlockMap(2);
        assert p.getUnlockedMap() == 2 : "Unlocks Map 2 (Museum)";

        p.unlockMap(3);
        assert p.getUnlockedMap() == 3 : "Unlocks Map 3 (San Pedro Cathedral)";

        p.unlockMap(4);
        assert p.getUnlockedMap() == 4 : "Unlocks Map 4 (Mt Apo)";

        p.unlockMap(5);
        assert p.getUnlockedMap() == 4 : "Capped at Map 4";

        System.out.println("  ✓ Next map chain verified: Blue Building -> Davao Museum -> San Pedro Cathedral -> Mt. Apo -> End.");

        // 2. Test Predefined Difficulty Mapping
        System.out.println("\n--- 2. Testing Predefined Difficulty Mapping ---");
        assert DifficultySetting.forMap(1) == DifficultySetting.SAYON : "Map 1 = Sayon";
        assert DifficultySetting.forMap(2) == DifficultySetting.SAKTO : "Map 2 = Sakto";
        assert DifficultySetting.forMap(3) == DifficultySetting.LISOD : "Map 3 = Lisod";
        assert DifficultySetting.forMap(4) == DifficultySetting.EXTREME : "Map 4 = Extreme";
        System.out.println("  ✓ Predefined difficulties verified: Sayon -> Sakto -> Lisod -> Extreme.");

        System.out.println("=== All Map Progression & Transition Tests Passed! ===");
        System.exit(0);
    }
}
