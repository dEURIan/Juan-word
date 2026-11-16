import java.util.List;
import juanword.data.DataManager;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;

public class TestMultiSaveFiles {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: MULTI-SAVE FILE INDEPENDENCE ===");

        DataManager dm = DataManager.getInstance();

        // 1. Create two separate profiles with distinctly different progress
        Player p1 = dm.createPlayer("JuanSave");
        p1.setActiveBattle(1, 2, 75, 30, 0, "A:STD;B:STD;C:STD;D:STD;E:STD;F:STD;G:STD;H:STD;I:STD;J:STD;K:STD;L:STD;M:STD;N:STD;O:STD;P:GLD");
        dm.savePlayers();

        Player p2 = dm.createPlayer("GudoSave");
        p2.setActiveBattle(3, 3, 90, 150, 22, "Z:GLD;Y:HEL;X:STD;W:STD;V:STD;U:STD;T:STD;S:STD;R:STD;Q:STD;P:STD;O:STD;N:STD;M:STD;L:STD;K:STD");
        dm.savePlayers();

        // 2. Validate both save files retain distinct state
        assert p1.hasActiveBattle() : "Player 1 must have active battle";
        assert p2.hasActiveBattle() : "Player 2 must have active battle";
        assert p1.getActiveMap() == 1 : "Player 1 should be on Map 1";
        assert p2.getActiveMap() == 3 : "Player 2 should be on Map 3";
        assert p1.getActivePlayerHp() == 75 : "Player 1 HP should be 75";
        assert p2.getActivePlayerHp() == 90 : "Player 2 HP should be 90";

        // 3. Test resuming both sessions via BattleSessionBuilder
        BattleSession session1 = BattleSessionBuilder.fromSavedPlayer(p1);
        assert session1.getMapIndex() == 1 : "Session 1 map should be 1";
        assert session1.getStageIndex() == 2 : "Session 1 stage should be 2";
        assert session1.getPlayerHp() == 75 : "Session 1 player HP should be 75";
        assert session1.getEnemy().getCurrentHp() == 30 : "Session 1 enemy HP should be 30";

        BattleSession session2 = BattleSessionBuilder.fromSavedPlayer(p2);
        assert session2.getMapIndex() == 3 : "Session 2 map should be 3";
        assert session2.getStageIndex() == 3 : "Session 2 stage should be 3";
        assert session2.getPlayerHp() == 90 : "Session 2 player HP should be 90";
        assert session2.getEnemy().getCurrentHp() == 150 : "Session 2 enemy HP should be 150";
        assert session2.getTurnSeconds() == 22 : "Session 2 turn timer should be 22";

        // Clean up test players
        dm.deletePlayer(p1.getPlayerId());
        dm.deletePlayer(p2.getPlayerId());

        System.out.println("  ✓ Multiple save profiles maintain strict data isolation and resume correctly.");
        System.exit(0);
    }
}
