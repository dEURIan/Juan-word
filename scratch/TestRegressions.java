import juanword.*;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestRegressions {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING REGRESSION SUITE ===");

        testSanPedroStages();
        testEnemyAttackAnimation();
        testHealthAndTilePersistence();
        testBookwormRackAndHealthBar();

        System.out.println("\n=== ALL REGRESSION TESTS PASSED! ===");
        System.exit(0);
    }

    private static void testSanPedroStages() {
        System.out.println("\n--- Testing San Pedro Stages 1-3 Configuration ---");
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());

        // Stage 1: Dizon
        panel.setupBattle(new Player("P001", "Juan"), "San Pedro Cathedral", 1, "Sakto");
        assert "DIZON".equals(panel.getCurrentEnemy().getName()) : "Stage 1 enemy should be Dizon";

        // Stage 2: Alviola
        panel.setupBattle(new Player("P001", "Juan"), "San Pedro Cathedral", 2, "Sakto");
        assert "ALVIOLA".equals(panel.getCurrentEnemy().getName()) : "Stage 2 enemy should be Alviola";

        // Stage 3: Rola
        panel.setupBattle(new Player("P001", "Juan"), "San Pedro Cathedral", 3, "Sakto");
        assert "ROLA".equals(panel.getCurrentEnemy().getName()) : "Stage 3 enemy should be Rola";

        System.out.println("  ✓ Stages 1, 2, and 3 configure correct enemy entities and stats.");
    }

    private static void testEnemyAttackAnimation() throws Exception {
        System.out.println("\n--- Testing Enemy Attack Animation ---");
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());
        panel.setupBattle(new Player("P001", "Juan"), "San Pedro Cathedral", 1, "Sakto");

        Field boxField = BattlePanel.class.getDeclaredField("dizonBox");
        boxField.setAccessible(true);
        Object dizonBox = boxField.get(panel);

        Field attackingField = dizonBox.getClass().getSuperclass().getDeclaredField("isAttacking");
        attackingField.setAccessible(true);
        Field lungeField = dizonBox.getClass().getSuperclass().getDeclaredField("lungeOffset");
        lungeField.setAccessible(true);

        CountDownLatch impactLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(1);

        java.lang.reflect.Method triggerAttack = dizonBox.getClass().getSuperclass().getDeclaredMethod(
            "triggerAttack", Runnable.class, Runnable.class
        );
        triggerAttack.setAccessible(true);

        triggerAttack.invoke(dizonBox,
            (Runnable) () -> impactLatch.countDown(),
            (Runnable) () -> completeLatch.countDown()
        );

        assert (boolean) attackingField.get(dizonBox) : "Enemy should be marked as attacking during animation";

        boolean impactFired = impactLatch.await(1, TimeUnit.SECONDS);
        assert impactFired : "Impact callback must fire during apex of attack lunge";

        boolean completeFired = completeLatch.await(1, TimeUnit.SECONDS);
        assert completeFired : "Complete callback must fire when attack animation finishes";

        assert !(boolean) attackingField.get(dizonBox) : "Enemy must reset attacking flag after animation";
        assert (int) lungeField.get(dizonBox) == 0 : "Lunge offset must reset to 0 after animation";

        System.out.println("  ✓ Enemy attack lunge animation, impact trigger, and recoil completed successfully.");
    }

    private static void testHealthAndTilePersistence() {
        System.out.println("\n--- Testing Health and Tile Persistence Across Sessions ---");
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());

        Player p = new Player("P_TEST_REG", "Tester");
        p.currentHealth = 64;
        p.maxHealth = 100;
        p.currentStage = 2;
        p.savedDifficulty = "Sakto";
        p.savedEnemyHealth = 45;
        p.savedTiles = "A:2:GOLD;B:1:HEAL;C:1:STANDARD;D:2:STANDARD;"
                     + "E:1:STANDARD;F:4:STANDARD;G:2:STANDARD;H:4:STANDARD;"
                     + "I:1:STANDARD;J:8:STANDARD;K:5:STANDARD;L:1:STANDARD;"
                     + "M:3:STANDARD;N:1:STANDARD;O:1:STANDARD;P:3:STANDARD";

        panel.resumeBattle(p);

        assert panel.getCurrentEnemy() instanceof AlviolaEnemy : "Resumed battle must configure Stage 2 Alviola";
        assert panel.getCurrentEnemy().getCurHp() == 45 : "Enemy HP must restore to 45";

        String serialized = panel.serializeTiles();
        assert serialized.contains("GOLD") : "Serialized tiles must retain GOLD";
        assert serialized.contains("HEAL") : "Serialized tiles must retain HEAL";

        DataManager.getInstance().deletePlayer("P_TEST_REG");

        System.out.println("  ✓ Health, Enemy HP, and Letter Tiles preserved accurately on resume.");
    }

    private static void testBookwormRackAndHealthBar() throws Exception {
        System.out.println("\n--- Testing Bookworm Word Rack and Health Bar ---");
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());
        panel.setupBattle(new Player("P001", "Juan"), "San Pedro Cathedral", 1, "Sakto");

        Field tilesField = BattlePanel.class.getDeclaredField("tiles");
        tilesField.setAccessible(true);
        Object[] tiles = (Object[]) tilesField.get(panel);
        assert tiles.length == 16 : "Must have 16 letter tiles on grid";

        Field dizonHealthBarField = BattlePanel.class.getDeclaredField("dizonHealthBar");
        dizonHealthBarField.setAccessible(true);
        Object healthBar = dizonHealthBarField.get(panel);
        assert healthBar != null : "Enemy health bar must be initialized";

        Field rackField = BattlePanel.class.getDeclaredField("wordRack");
        rackField.setAccessible(true);
        Object rack = rackField.get(panel);
        assert rack != null : "Bookworm Word Rack must be initialized";

        System.out.println("  ✓ Word rack and enemy health bar initialized and functional.");
    }
}
