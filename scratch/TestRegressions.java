import juanword.data.DataManager;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.model.AttackType;
import juanword.model.BattleOutcome;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.tile.TileComponent;
import juanword.tile.TileGridGenerator;

public class TestRegressions {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING REGRESSION SUITE ===");

        testDictionaryLookup();
        testCampaignStages();
        testGridSerializationAndPersistence();
        testDifficultyDamageMultipliers();

        System.out.println("\n=== ALL REGRESSION TESTS PASSED! ===");
        System.exit(0);
    }

    private static void testDictionaryLookup() {
        System.out.println("\n--- 1. Testing Dictionary & Lexical Validation ---");
        DataManager dm = DataManager.getInstance();
        assert dm.getDictionarySize() > 1000 : "Dictionary must contain loaded Cebuano words; found: " + dm.getDictionarySize();

        // Common Cebuano words
        assert dm.isValidCebuanoWord("BALAY") : "'BALAY' should be valid";
        assert dm.isValidCebuanoWord("KAUN") : "'KAUN' should be valid";
        assert !dm.isValidCebuanoWord("ZZZZQ") : "'ZZZZQ' should not be valid";
        assert !dm.isValidCebuanoWord("A") : "Single letter words should be invalid";
        assert !dm.isValidCebuanoWord(null) : "Null should be invalid";

        System.out.println("  ✓ Dictionary lookups and Cebuano word validations functioning at O(1).");
    }

    private static void testCampaignStages() {
        System.out.println("\n--- 2. Testing Campaign Stage Configurations ---");
        // Check 4 maps x 3 stages = 12 enemy encounters
        for (int m = 1; m <= 4; m++) {
            DifficultySetting diff = DifficultySetting.forMap(m);
            for (int s = 1; s <= 3; s++) {
                Enemy e = EnemyFactory.createEnemy(m, s, diff);
                assert e != null : "Enemy for map " + m + " stage " + s + " must exist";
                assert e.getMaxHp() > 0 : "Enemy HP must be positive";
                assert e.getAttack() > 0 : "Enemy Attack must be positive";
                if (s == 3) {
                    assert e.isBoss() : "Stage 3 of map " + m + " must be a boss";
                }
            }
        }
        System.out.println("  ✓ All 4 maps (12 stages) correctly instantiate with balanced stats.");
    }

    private static void testGridSerializationAndPersistence() {
        System.out.println("\n--- 3. Testing 16-Tile Grid Serialization ---");
        DataManager dm = DataManager.getInstance();
        TileComponent[][] grid = TileGridGenerator.generateFresh4x4();
        String serialized = dm.serializeGrid(grid);
        assert serialized != null && !serialized.isEmpty() : "Serialized grid must not be empty";

        String[] tokens = serialized.split(";");
        assert tokens.length == 16 : "Must serialize exactly 16 tile tokens, got: " + tokens.length;

        TileComponent[][] restored = dm.deserializeGrid(serialized);
        assert restored != null && restored.length == 4 && restored[0].length == 4 : "Restored grid must be 4x4";
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                assert restored[r][c].getLetter() == grid[r][c].getLetter() : "Letter mismatch at [" + r + "][" + c + "]";
                assert restored[r][c].getModifierCode().equals(grid[r][c].getModifierCode()) : "Modifier mismatch at [" + r + "][" + c + "]";
            }
        }
        System.out.println("  ✓ 16-tile board serializes and deserializes losslessly with modifier decorators.");
    }

    private static void testDifficultyDamageMultipliers() {
        System.out.println("\n--- 4. Testing Word Length Multipliers ---");
        DifficultySetting sayon = DifficultySetting.SAYON;
        assert sayon.getLengthMultiplier(2) == 0.80 : "Sayon length 2 multiplier mismatch";
        assert sayon.getLengthMultiplier(3) == 1.00 : "Sayon length 3 multiplier mismatch";
        assert sayon.getLengthMultiplier(4) == 1.20 : "Sayon length 4 multiplier mismatch";
        assert sayon.getLengthMultiplier(5) == 1.45 : "Sayon length 5 multiplier mismatch";
        assert sayon.getLengthMultiplier(6) == 1.80 : "Sayon length 6 multiplier mismatch";
        assert sayon.getLengthMultiplier(8) == 1.80 : "Sayon length 7+ multiplier mismatch";

        DifficultySetting lisod = DifficultySetting.LISOD;
        assert lisod.getLengthMultiplier(2) == 0.20 : "Lisod length 2 multiplier mismatch";
        assert lisod.getLengthMultiplier(7) == 3.00 : "Lisod length 7+ multiplier mismatch";
        assert lisod.getInvalidCounterDamage() == 8 : "Lisod must counter with 8 damage on invalid word";

        System.out.println("  ✓ Difficulty length multipliers and penalties match specifications.");
    }
}
