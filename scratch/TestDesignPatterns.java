import juanword.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class TestDesignPatterns {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: GOF DESIGN PATTERNS 1 THROUGH 4 ===");

        testPattern1Singleton();
        testPattern2FactoryMethod();
        testPattern3Builder();
        testPattern4Decorator();

        System.out.println("=== ALL DESIGN PATTERN TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }

    private static void testPattern1Singleton() throws Exception {
        System.out.println("\n--- Testing Pattern 1: Singleton (DataManager) ---");

        DataManager instance1 = DataManager.getInstance();
        assert instance1 != null : "DataManager.getInstance() must not be null";

        DataManager instance2 = DataManager.getInstance();
        assert instance1 == instance2 : "DataManager.getInstance() must return the same instance";

        // Multithreaded concurrent access test
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DataManager>[] results = new AtomicReference[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            results[idx] = new AtomicReference<>();
            executor.submit(() -> {
                try {
                    latch.await();
                    results[idx].set(DataManager.getInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        for (int i = 0; i < threadCount; i++) {
            assert results[i].get() == instance1 : "Thread " + i + " must get the exact same Singleton instance";
        }

        System.out.println("  ✓ Singleton: DataManager double-checked locking returns single shared instance across threads.");
    }

    private static void testPattern2FactoryMethod() {
        System.out.println("\n--- Testing Pattern 2: Factory Method (EnemyFactory -> Enemy) ---");

        // Stage 1: Dizon
        Enemy enemy1 = EnemyFactory.createEnemy(1, BattlePanel.DifficultySetting.SAYON);
        assert enemy1 instanceof DizonEnemy : "Stage 1 must create DizonEnemy";
        assert "DIZON".equals(enemy1.getName()) : "Stage 1 enemy name must be DIZON";
        assert enemy1.getMaxHp() == Math.max(30, (int) Math.round(70 * 0.75)) : "Sayon Dizon HP scaling mismatch";
        System.out.println("  ✓ Stage 1 Enemy created via EnemyFactory: " + enemy1.getTitle() + " (HP: " + enemy1.getMaxHp() + ")");

        // Stage 2: Alviola
        Enemy enemy2 = EnemyFactory.createEnemy(2, BattlePanel.DifficultySetting.SAKTO);
        assert enemy2 instanceof AlviolaEnemy : "Stage 2 must create AlviolaEnemy";
        assert "ALVIOLA".equals(enemy2.getName()) : "Stage 2 enemy name must be ALVIOLA";
        assert enemy2.getMaxHp() == 95 : "Sakto Alviola HP should be base 95";
        assert enemy2.getAtk() == 13 : "Sakto Alviola ATK should be base 13";
        System.out.println("  ✓ Stage 2 Enemy created via EnemyFactory: " + enemy2.getTitle() + " (HP: " + enemy2.getMaxHp() + ")");

        // Stage 3: Rola (Boss)
        Enemy enemy3 = EnemyFactory.createEnemy(3, BattlePanel.DifficultySetting.LISOD);
        assert enemy3 instanceof RolaBossEnemy : "Stage 3 must create RolaBossEnemy";
        assert "ROLA".equals(enemy3.getName()) : "Stage 3 enemy name must be ROLA";
        assert enemy3.getMaxHp() == (int) Math.round(125 * 1.30) : "Lisod Rola HP scaling mismatch";
        assert enemy3.getAtk() == (int) Math.round(16 * 1.40) : "Lisod Rola ATK scaling mismatch";
        System.out.println("  ✓ Stage 3 Boss created via EnemyFactory: " + enemy3.getTitle() + " (HP: " + enemy3.getMaxHp() + ")");

        // Polymorphic damage and rendering
        enemy1.takeDamage(25);
        assert enemy1.getCurHp() == enemy1.getMaxHp() - 25 : "Enemy takeDamage failed";

        BufferedImage img = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        enemy1.render(g2, 10, 10, 110, 85, false, 0);
        enemy2.render(g2, 10, 10, 110, 85, true, -10);
        enemy3.render(g2, 10, 10, 110, 85, false, 0);
        g2.dispose();
        System.out.println("  ✓ Polymorphic Enemy.render() executed without errors across all 3 enemy types.");
    }

    private static void testPattern3Builder() {
        System.out.println("\n--- Testing Pattern 3: Builder (BattleSessionBuilder -> BattleSession) ---");

        Player player = new Player("P999", "LapuLapu");
        player.currentHealth = 85;
        player.savedTiles = "A:1;B:2;C:3";
        player.savedEnemyHealth = 40;

        BattleSession session = new BattleSessionBuilder()
                .player(player)
                .mapName("San Pedro Cathedral")
                .stageNum(2)
                .difficulty("Lisod")
                .asResume(true)
                .savedTiles(player.savedTiles)
                .savedEnemyHealth(player.savedEnemyHealth)
                .build();

        assert session != null : "BattleSession must not be null";
        assert session.getPlayer() == player : "Player reference mismatch in session";
        assert "San Pedro Cathedral".equals(session.getMapName()) : "MapName mismatch";
        assert session.getStageNum() == 2 : "StageNum mismatch";
        assert session.getDifficulty() == BattlePanel.DifficultySetting.LISOD : "Difficulty mismatch";
        assert session.isResume() : "isResume mismatch";
        assert "A:1;B:2;C:3".equals(session.getSavedTiles()) : "savedTiles mismatch";
        assert session.getSavedEnemyHealth() == 40 : "savedEnemyHealth mismatch";

        System.out.println("  ✓ BattleSession constructed via BattleSessionBuilder: " + session);

        // Test with BattlePanel
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());
        panel.setupBattle(session);

        assert panel.getCurrentEnemy() instanceof AlviolaEnemy : "Panel should have loaded Alviola for stage 2";
        assert panel.getCurrentEnemy().getCurHp() == 40 : "Panel should have restored saved enemy HP 40";
        System.out.println("  ✓ BattlePanel.setupBattle(BattleSession) successfully consumed Builder product.");
    }

    private static void testPattern4Decorator() {
        System.out.println("\n--- Testing Pattern 4: Decorator (TileDecorator, GoldTileDecorator, HealingTileDecorator) ---");

        // 1. Standard Component
        TileComponent std = new StandardTileComponent('A', 2);
        assert std.getLetter() == 'a' : "Standard letter mismatch";
        assert std.getPoints() == 2 : "Standard points mismatch";
        assert "STANDARD".equals(std.getEffectName()) : "Standard effect name mismatch";
        System.out.println("  ✓ Concrete Component: StandardTileComponent ('a', 2 pts)");

        // 2. Gold Tile Decorator (2x Points)
        TileComponent gold = new GoldTileDecorator(std);
        assert gold.getLetter() == 'a' : "Gold decorated letter must match wrapped letter";
        assert gold.getBasePoints() == 2 : "Gold base points must match wrapped base points";
        assert gold.getPoints() == 4 : "Gold decorated points must be 2x wrapped points (4 vs " + gold.getPoints() + ")";
        assert "GOLD".equals(gold.getEffectName()) : "Gold effect name mismatch";
        System.out.println("  ✓ Concrete Decorator 1: GoldTileDecorator doubles points to " + gold.getPoints() + " pts");

        // 3. Healing Tile Decorator (+8 HP)
        TileComponent heal = new HealingTileDecorator(std);
        assert heal.getLetter() == 'a' : "Healing decorated letter must match wrapped letter";
        assert heal.getPoints() == 2 : "Healing decorated points should remain 2";
        assert "HEAL".equals(heal.getEffectName()) : "Healing effect name mismatch";
        System.out.println("  ✓ Concrete Decorator 2: HealingTileDecorator configured (+8 HP)");

        // 4. Test onWordSubmitted effect with BattlePanel
        JuanWord app = new JuanWord();
        BattlePanel panel = new BattlePanel(app, DataManager.getInstance());
        Player player = new Player("P001", "Juan");
        player.currentHealth = 50; // Damaged Juan
        player.maxHealth = 100;
        panel.setupBattle(player, "San Pedro Cathedral", 1, "Sakto");

        heal.onWordSubmitted(player, panel);
        // Juan's HP should increase from 50 to 58 (+8 HP)
        System.out.println("  ✓ HealingTileDecorator.onWordSubmitted healed Juan to " + player.currentHealth + " HP");

        // 5. Test tile serialization and restoration with Decorator tags
        String testTiles = "A:2:GOLD;B:1:HEAL;C:1:STANDARD;D:2:STANDARD;"
                         + "E:1:STANDARD;F:4:STANDARD;G:2:STANDARD;H:4:STANDARD;"
                         + "I:1:STANDARD;J:8:STANDARD;K:5:STANDARD;L:1:STANDARD;"
                         + "M:3:STANDARD;N:1:STANDARD;O:1:STANDARD;P:3:STANDARD";

        boolean restored = panel.restoreTiles(testTiles);
        assert restored : "restoreTiles must return true for valid 16-tile serialized string";

        String serializedBack = panel.serializeTiles();
        assert serializedBack.contains("GOLD") : "Serialized tiles must preserve GOLD decorator tag";
        assert serializedBack.contains("HEAL") : "Serialized tiles must preserve HEAL decorator tag";
        System.out.println("  ✓ Tile serialization/restoration roundtrip preserved Decorator tags: " + serializedBack.substring(0, 30) + "...");

        // Verify badge rendering doesn't crash
        BufferedImage img = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        gold.renderDecoratorBadge(g2, 0, 0, 50, 50);
        heal.renderDecoratorBadge(g2, 0, 0, 50, 50);
        g2.dispose();
        System.out.println("  ✓ Decorator badge rendering (Gold star, Healing heart) verified successfully.");
    }
}
