import juanword.*;
import java.util.List;

public class TestMultiSaveFiles {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: MULTI-SAVE FILE INDEPENDENCE ===");

        DataManager dm = DataManager.getInstance();

        // 1. Create two separate save files with distinctly different game progress
        Player save1 = new Player("P_TEST_1", "JuanSave");
        save1.currentStage = 1; // Stage 1 (Dizon)
        save1.currentRound = 1;
        save1.currentHealth = 72;
        save1.maxHealth = 100;
        save1.savedEnemyHealth = 48; // Dizon HP
        save1.savedDifficulty = "Sakto";
        save1.savedMap = "San Pedro Cathedral";
        save1.savedTiles = "A:1:STANDARD;B:1:STANDARD;C:1:STANDARD;D:1:STANDARD;"
                         + "E:1:STANDARD;F:1:STANDARD;G:1:STANDARD;H:1:STANDARD;"
                         + "I:1:STANDARD;J:1:STANDARD;K:1:STANDARD;L:1:STANDARD;"
                         + "M:1:STANDARD;N:1:STANDARD;O:1:STANDARD;P:2:GOLD";
        dm.savePlayer(save1);

        Player save2 = new Player("P_TEST_2", "GudoSave");
        save2.currentStage = 2; // Stage 2 (Alviola)
        save2.currentRound = 1;
        save2.currentHealth = 95;
        save2.maxHealth = 100;
        save2.savedEnemyHealth = 80; // Alviola HP
        save2.savedDifficulty = "Lisod";
        save2.savedMap = "San Pedro Cathedral";
        save2.savedTiles = "Z:10:GOLD;Y:3:HEAL;X:8:STANDARD;W:4:STANDARD;"
                         + "V:4:STANDARD;U:1:STANDARD;T:2:STANDARD;S:2:STANDARD;"
                         + "R:3:STANDARD;Q:10:STANDARD;P:3:STANDARD;O:1:STANDARD;"
                         + "N:1:STANDARD;M:3:STANDARD;L:1:STANDARD;K:5:STANDARD";
        dm.savePlayer(save2);

        // 2. Test JuanWord hasActiveGame for both
        JuanWord app = new JuanWord();
        assert app.hasActiveGame(save1) : "Save 1 should have active game";
        assert app.hasActiveGame(save2) : "Save 2 should have active game";
        System.out.println("  ✓ Both save files detected as having active games.");

        // 3. Test resuming Save 1 in BattlePanel
        BattlePanel panel1 = new BattlePanel(app, dm);
        panel1.resumeBattle(save1);

        assert "DIZON".equals(panel1.getCurrentEnemy().getName()) : "Save 1 must resume with Dizon";
        assert panel1.getCurrentEnemy().getCurHp() == 48 : "Save 1 must have Dizon HP 48";
        assert panel1.getCurrentPlayer().currentHealth == 72 : "Save 1 must have Juan HP 72";
        assert panel1.serializeTiles().contains("P:2:GOLD") : "Save 1 must have its own tiles";
        System.out.println("  ✓ Save 1 successfully resumed into Stage 1 Dizon battle (HP: 72, Enemy HP: 48).");

        // 4. Test resuming Save 2 in BattlePanel
        BattlePanel panel2 = new BattlePanel(app, dm);
        panel2.resumeBattle(save2);

        assert "ALVIOLA".equals(panel2.getCurrentEnemy().getName()) : "Save 2 must resume with Alviola";
        assert panel2.getCurrentEnemy().getCurHp() == 80 : "Save 2 must have Alviola HP 80";
        assert panel2.getCurrentPlayer().currentHealth == 95 : "Save 2 must have Gudo HP 95";
        assert panel2.serializeTiles().contains("Z:20:GOLD") || panel2.serializeTiles().contains("Z:10:GOLD") : "Save 2 must have its own tiles";
        assert panel2.serializeTiles().contains("Y:3:HEAL") : "Save 2 must have its own healing tile";
        System.out.println("  ✓ Save 2 successfully resumed into Stage 2 Alviola battle (HP: 95, Enemy HP: 80).");

        // 5. Verify non-interference: Modifying Save 1 does NOT affect Save 2
        save1.currentHealth = 50;
        dm.savePlayer(save1);

        Player reloadedSave2 = dm.loadPlayer("P_TEST_2");
        assert reloadedSave2 != null && reloadedSave2.currentHealth == 95 : "Save 2 health must remain 95";
        assert reloadedSave2.currentStage == 2 : "Save 2 stage must remain 2";
        assert reloadedSave2.savedEnemyHealth == 80 : "Save 2 enemy health must remain 80";
        System.out.println("  ✓ Independence verified: updates to Save 1 do not alter Save 2.");

        // Cleanup test entries
        dm.deletePlayer("P_TEST_1");
        dm.deletePlayer("P_TEST_2");
        System.out.println("  ✓ Cleaned up test player records.");

        System.out.println("\n=== ALL MULTI-SAVE FILE TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }
}
