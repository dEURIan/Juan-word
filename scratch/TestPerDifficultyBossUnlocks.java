import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class TestPerDifficultyBossUnlocks {

    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: PER-DIFFICULTY BOSS UNLOCKS ===");

        testDefaultUnlocks();
        testUnlockBossOnSpecificDifficulty();
        testUnlockPersistenceAcrossSaveAndReload();
        testReplayingStage1DoesNotRelockBoss();
        testDynamicUIStageCardRefresh();
        testBackwardCompatibilityOldCsv();

        System.out.println("\n=== ALL PER-DIFFICULTY BOSS UNLOCK TESTS PASSED! ===");
        System.exit(0);
    }

    private static void testDefaultUnlocks() {
        System.out.println("\n--- 1. Testing Default Unlocks (Fresh Player) ---");
        Player p = new Player("P_TEST_FRESH", "TestFresh");

        // By default, Stage 1 (Dizon) is unlocked for all difficulties
        assert p.getMaxUnlockedStage("Sayon") == 1 : "Sayon should default to Stage 1";
        assert p.getMaxUnlockedStage("Sakto") == 1 : "Sakto should default to Stage 1";
        assert p.getMaxUnlockedStage("Lisod") == 1 : "Lisod should default to Stage 1";

        // Boss (Stage 3 Rola) should NOT be unlocked on any difficulty yet
        assert !p.isBossUnlocked("Sayon") : "Boss should not be unlocked on Sayon";
        assert !p.isBossUnlocked("Sakto") : "Boss should not be unlocked on Sakto";
        assert !p.isBossUnlocked("Lisod") : "Boss should not be unlocked on Lisod";

        System.out.println("  ✓ Default state: Stage 1 unlocked, Boss locked for Sayon, Sakto, and Lisod.");
    }

    private static void testUnlockBossOnSpecificDifficulty() {
        System.out.println("\n--- 2. Testing Boss Unlock on Specific Difficulty (Lisod) ---");
        Player p = new Player("P_TEST_LISOD", "TestLisodMaster");

        // Defeat Stage 1 (Dizon) on Lisod
        p.recordStageUnlock("Lisod", 2);
        assert p.getMaxUnlockedStage("Lisod") == 2 : "Lisod should now be at Stage 2";
        assert p.getMaxUnlockedStage("Sayon") == 1 : "Sayon should still be at Stage 1";
        assert p.getMaxUnlockedStage("Sakto") == 1 : "Sakto should still be at Stage 1";
        assert !p.isBossUnlocked("Lisod") : "Boss not unlocked yet at Stage 2";

        // Defeat Stage 2 (Alviola) on Lisod -> Boss Rola unlocked!
        p.recordStageUnlock("Lisod", 3);
        assert p.getMaxUnlockedStage("Lisod") == 3 : "Lisod should now have Stage 3 unlocked";
        assert p.isBossUnlocked("Lisod") : "Boss Rola must now be unlocked for Lisod!";

        // Ensure other difficulties remain locked
        assert p.getMaxUnlockedStage("Sayon") == 1 : "Sayon must remain at Stage 1";
        assert p.getMaxUnlockedStage("Sakto") == 1 : "Sakto must remain at Stage 1";
        assert !p.isBossUnlocked("Sayon") : "Boss must remain locked for Sayon";
        assert !p.isBossUnlocked("Sakto") : "Boss must remain locked for Sakto";

        System.out.println("  ✓ Boss Rola unlocked exclusively for Lisod, while Sayon & Sakto stay locked.");
    }

    private static void testUnlockPersistenceAcrossSaveAndReload() {
        System.out.println("\n--- 3. Testing Boss Unlock Persistence Across Save & Reload ---");
        DataManager dm = DataManager.getInstance();

        String testId = "P_TEST_PERSIST";
        Player p = new Player(testId, "PersistPlayer");
        p.recordStageUnlock("Lisod", 3); // Unlock boss on Lisod
        p.recordStageUnlock("Sayon", 2); // Unlock stage 2 on Sayon
        // Sakto remains at stage 1
        dm.savePlayer(p);

        // Reload from disk
        Player loaded = dm.loadPlayer(testId);
        assert loaded != null : "Saved player should be loadable";
        assert loaded.getMaxUnlockedStage("Lisod") == 3 : "Loaded player must preserve Lisod Stage 3 (Boss)";
        assert loaded.isBossUnlocked("Lisod") : "Loaded player must have Boss unlocked on Lisod";
        assert loaded.getMaxUnlockedStage("Sayon") == 2 : "Loaded player must preserve Sayon Stage 2";
        assert !loaded.isBossUnlocked("Sayon") : "Loaded player must keep Boss locked on Sayon";
        assert loaded.getMaxUnlockedStage("Sakto") == 1 : "Loaded player must preserve Sakto Stage 1";
        assert !loaded.isBossUnlocked("Sakto") : "Loaded player must keep Boss locked on Sakto";

        // Clean up test player
        dm.deletePlayer(testId);
        System.out.println("  ✓ Per-difficulty boss unlock persisted and restored accurately from CSV.");
    }

    private static void testReplayingStage1DoesNotRelockBoss() {
        System.out.println("\n--- 4. Testing Replay: Replaying Stage 1 Never Re-locks Boss ---");
        Player p = new Player("P_TEST_REPLAY", "ReplayPlayer");
        p.recordStageUnlock("Lisod", 3); // Unlocks boss
        assert p.isBossUnlocked("Lisod") : "Boss initially unlocked on Lisod";

        // Replaying Stage 1 on Lisod:
        p.currentStage = 1;
        p.currentRound = 1;
        p.recordStageUnlock("Lisod", 1); // Should not downgrade!
        assert p.getMaxUnlockedStage("Lisod") == 3 : "Replaying Stage 1 must not downgrade unlocked stage";
        assert p.isBossUnlocked("Lisod") : "Boss Rola must remain unlocked after replaying Stage 1";

        System.out.println("  ✓ Replaying Stage 1 preserves boss unlock permanently.");
    }

    private static void testDynamicUIStageCardRefresh() throws Exception {
        System.out.println("\n--- 5. Testing Dynamic UI Stage Card Refresh on Difficulty Switch ---");
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                Player p = new Player("P_TEST_UI", "UIPlayer");
                p.recordStageUnlock("Lisod", 3); // Boss unlocked on Lisod
                // Sayon has only Stage 1 unlocked

                JPanel content = app.createSanPedroStagesContent(p, () -> {}, (stage, diff) -> {});
                assert content != null : "Content panel must not be null";

                // Locate center panel which contains diffRow and stagesList
                BorderLayout bl = (BorderLayout) content.getLayout();
                JPanel centerPanel = (JPanel) bl.getLayoutComponent(BorderLayout.CENTER);
                BorderLayout cbl = (BorderLayout) centerPanel.getLayout();

                JPanel diffRow = (JPanel) cbl.getLayoutComponent(BorderLayout.NORTH);
                JPanel stagesList = (JPanel) cbl.getLayoutComponent(BorderLayout.CENTER);

                // Find difficulty buttons: "Sayon", "Sakto", "Lisod"
                FlatGameButton sayonBtn = null;
                FlatGameButton lisodBtn = null;
                for (Component comp : diffRow.getComponents()) {
                    if (comp instanceof FlatGameButton) {
                        FlatGameButton b = (FlatGameButton) comp;
                        if (b.getText().contains("Sayon")) sayonBtn = b;
                        if (b.getText().contains("Lisod")) lisodBtn = b;
                    }
                }
                assert sayonBtn != null : "Sayon button must exist";
                assert lisodBtn != null : "Lisod button must exist";

                // Click "Sayon"
                sayonBtn.doClick();
                // Check Stage 3 card in stagesList: on Sayon, Stage 3 (Rola) should be LOCKED
                Component[] cards = stagesList.getComponents();
                assert cards.length == 3 : "There must be 3 stage cards";
                JPanel stage3CardSayon = (JPanel) cards[2];
                BorderLayout s3Layout = (BorderLayout) stage3CardSayon.getLayout();
                FlatGameButton stage3BtnSayon = (FlatGameButton) s3Layout.getLayoutComponent(BorderLayout.EAST);
                assert !stage3BtnSayon.isEnabled() : "On Sayon, Stage 3 (Boss) must be locked";
                assert stage3BtnSayon.getText().contains("Lock") : "Stage 3 button on Sayon must show Lock";

                // Click "Lisod"
                lisodBtn.doClick();
                // Check Stage 3 card in stagesList: on Lisod, Stage 3 (Rola) should be UNLOCKED!
                cards = stagesList.getComponents();
                JPanel stage3CardLisod = (JPanel) cards[2];
                BorderLayout s3LayoutLisod = (BorderLayout) stage3CardLisod.getLayout();
                FlatGameButton stage3BtnLisod = (FlatGameButton) s3LayoutLisod.getLayoutComponent(BorderLayout.EAST);
                assert stage3BtnLisod.isEnabled() : "On Lisod, Stage 3 (Boss) must be UNLOCKED";
                assert stage3BtnLisod.getText().contains("Sukla") : "Stage 3 button on Lisod must show Sukla ▶";

                // Click back to "Sayon"
                sayonBtn.doClick();
                cards = stagesList.getComponents();
                JPanel stage3CardSayonAgain = (JPanel) cards[2];
                FlatGameButton stage3BtnSayonAgain = (FlatGameButton) ((BorderLayout) stage3CardSayonAgain.getLayout()).getLayoutComponent(BorderLayout.EAST);
                assert !stage3BtnSayonAgain.isEnabled() : "Switching back to Sayon must re-lock Stage 3";

                System.out.println("  ✓ Stage cards dynamically toggle between locked and unlocked based on selected difficulty!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void testBackwardCompatibilityOldCsv() {
        System.out.println("\n--- 6. Testing Backward Compatibility with Old CSV Lines ---");
        // Old CSV without the 3 new trailing unlock columns
        String oldCsvLine = "P999,OldPlayer,2,1,100,20,10,90,100,2026-10-30 12:00,A:1:STANDARD,Sakto,50,San Pedro Cathedral";
        Player p = Player.fromCsv(oldCsvLine);
        assert p != null : "Old CSV line must parse successfully";
        assert p.currentStage == 2 : "currentStage should be 2";
        // Backward-compatible fallback sets all unlocks to at least currentStage (2)
        assert p.getMaxUnlockedStage("Sayon") >= 2 : "Fallback should initialize Sayon to at least currentStage";
        assert p.getMaxUnlockedStage("Sakto") >= 2 : "Fallback should initialize Sakto to at least currentStage";
        assert p.getMaxUnlockedStage("Lisod") >= 2 : "Fallback should initialize Lisod to at least currentStage";

        System.out.println("  ✓ Legacy CSV records parsed with seamless backward-compatible defaults.");
    }
}
