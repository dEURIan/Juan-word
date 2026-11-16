import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import juanword.data.DataManager;
import juanword.enemy.AltarBoyEnemy;
import juanword.enemy.AlviolaBossEnemy;
import juanword.enemy.DizonBossEnemy;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.enemy.GuardEnemy;
import juanword.enemy.JanitorBossEnemy;
import juanword.enemy.KarabawEnemy;
import juanword.enemy.LolaEnemy;
import juanword.enemy.MaleStudentEnemy;
import juanword.enemy.MonkeyEnemy;
import juanword.enemy.PaintingEnemy;
import juanword.enemy.PorterBossEnemy;
import juanword.model.BattleContext;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.tile.GoldTileDecorator;
import juanword.tile.HealingTileDecorator;
import juanword.tile.StandardTileComponent;
import juanword.tile.TileComponent;

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

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        @SuppressWarnings("unchecked")
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
            assert results[i].get() == instance1 : "Concurrent thread " + i + " got a different DataManager instance!";
        }

        System.out.println("  ✓ DataManager successfully verified as a thread-safe Singleton (double-checked locking).");
    }

    private static void testPattern2FactoryMethod() {
        System.out.println("\n--- Testing Pattern 2: Factory Method (EnemyFactory) ---");

        // Map 1 Enemies
        Enemy e11 = EnemyFactory.createEnemy(1, 1, DifficultySetting.SAYON);
        assert e11 instanceof GuardEnemy : "Map 1 Stage 1 must be GuardEnemy";
        Enemy e12 = EnemyFactory.createEnemy(1, 2, DifficultySetting.SAYON);
        assert e12 instanceof MaleStudentEnemy : "Map 1 Stage 2 must be MaleStudentEnemy";
        Enemy e13 = EnemyFactory.createEnemy(1, 3, DifficultySetting.SAYON);
        assert e13 instanceof DizonBossEnemy : "Map 1 Stage 3 must be DizonBossEnemy";
        assert e13.isBoss() : "Dizon must be marked as boss";

        // Map 2 Enemies
        Enemy e21 = EnemyFactory.createEnemy(2, 1, DifficultySetting.SAKTO);
        assert e21 instanceof PaintingEnemy : "Map 2 Stage 1 must be PaintingEnemy";
        Enemy e22 = EnemyFactory.createEnemy(2, 2, DifficultySetting.SAKTO);
        assert e22 instanceof PaintingEnemy : "Map 2 Stage 2 must be PaintingEnemy";
        Enemy e23 = EnemyFactory.createEnemy(2, 3, DifficultySetting.SAKTO);
        assert e23 instanceof JanitorBossEnemy : "Map 2 Stage 3 must be JanitorBossEnemy";

        // Map 3 Enemies
        Enemy e31 = EnemyFactory.createEnemy(3, 1, DifficultySetting.LISOD);
        assert e31 instanceof LolaEnemy : "Map 3 Stage 1 must be LolaEnemy";
        Enemy e32 = EnemyFactory.createEnemy(3, 2, DifficultySetting.LISOD);
        assert e32 instanceof AltarBoyEnemy : "Map 3 Stage 2 must be AltarBoyEnemy";
        Enemy e33 = EnemyFactory.createEnemy(3, 3, DifficultySetting.LISOD);
        assert e33 instanceof AlviolaBossEnemy : "Map 3 Stage 3 must be AlviolaBossEnemy";

        // Map 4 Enemies
        Enemy e41 = EnemyFactory.createEnemy(4, 1, DifficultySetting.EXTREME);
        assert e41 instanceof MonkeyEnemy : "Map 4 Stage 1 must be MonkeyEnemy";
        Enemy e42 = EnemyFactory.createEnemy(4, 2, DifficultySetting.EXTREME);
        assert e42 instanceof KarabawEnemy : "Map 4 Stage 2 must be KarabawEnemy";
        Enemy e43 = EnemyFactory.createEnemy(4, 3, DifficultySetting.EXTREME);
        assert e43 instanceof PorterBossEnemy : "Map 4 Stage 3 must be PorterBossEnemy";
        assert e43.isBoss() : "Porter must be marked as boss";

        assert EnemyFactory.mapCount() == 4 : "Must have 4 maps";
        assert EnemyFactory.stagesPerMap() == 3 : "Must have 3 stages per map";

        System.out.println("  ✓ EnemyFactory successfully instantiates all 11 concrete enemies across 4 maps.");
    }

    private static void testPattern3Builder() {
        System.out.println("\n--- Testing Pattern 3: Builder (BattleSessionBuilder) ---");

        Player player = new Player(999, "LapuLapu");

        BattleSession session = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(3)
            .atStage(2)
            .withSavedHealth(85, 90)
            .withRemainingTimer(18)
            .build();

        assert session != null : "Built session must not be null";
        assert session.getPlayer() == player : "Player reference must match";
        assert session.getMapIndex() == 3 : "Map index must be 3";
        assert session.getStageIndex() == 2 : "Stage index must be 2";
        assert session.getDifficulty() == DifficultySetting.LISOD : "Difficulty must be LISOD";
        assert session.getPlayerHp() == 85 : "Saved player HP must be 85";
        assert session.getEnemy().getCurrentHp() == 90 : "Saved enemy HP must be 90";
        assert session.getTurnSeconds() == 18 : "Saved turn seconds must be 18";
        assert session.getEnemy() instanceof AltarBoyEnemy : "Enemy must be AltarBoyEnemy";

        System.out.println("  ✓ BattleSessionBuilder successfully constructs tailored BattleSession.");
    }

    private static void testPattern4Decorator() {
        System.out.println("\n--- Testing Pattern 4: Decorator (Tile Decorators) ---");

        TileComponent std = new StandardTileComponent('A');
        assert std.getLetter() == 'A' : "Letter mismatch";
        int basePoints = std.getPoints();
        assert "STD".equals(std.getModifierCode()) : "Modifier code must be STD";

        // Gold decorator
        TileComponent gold = new GoldTileDecorator(std);
        assert gold.getLetter() == 'A' : "Letter should remain unchanged";
        assert gold.getPoints() == basePoints * 2 : "Gold tile must double points";
        assert "GLD".equals(gold.getModifierCode()) : "Modifier code must be GLD";

        // Healing decorator
        TileComponent heal = new HealingTileDecorator(std);
        assert heal.getLetter() == 'A' : "Letter should remain unchanged";
        assert heal.getPoints() == basePoints : "Healing tile retains standard points";
        assert "HEL".equals(heal.getModifierCode()) : "Modifier code must be HEL";

        // Functional effects
        Player player = new Player(998, "TestDec");
        player.setMaxHp(100);
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(1)
            .atStage(1)
            .withSavedHealth(50, 30)
            .build();

        BattleContext ctx = new BattleContext(session, "A", null);

        // Gold effect adds score
        int scoreBefore = player.getScore();
        gold.onWordSubmitted(ctx);
        assert player.getScore() > scoreBefore : "Gold tile must award score";

        // Healing effect adds HP
        int hpBefore = session.getPlayerHp();
        heal.onWordSubmitted(ctx);
        assert session.getPlayerHp() == hpBefore + 8 : "Healing tile must heal +8 HP";

        // Verify badge and overlay rendering does not crash
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        std.renderTileOverlay(g2, 0, 0, 64);
        gold.renderTileOverlay(g2, 0, 0, 64);
        heal.renderTileOverlay(g2, 0, 0, 64);
        gold.renderBadge(g2, 0, 0, 64);
        heal.renderBadge(g2, 0, 0, 64);
        g2.dispose();

        // Distinct decorator tile colors and border colors
        assert std.getTileColor() != null : "Standard tile must have a tile color";
        assert gold.getTileColor() != null && !gold.getTileColor().equals(std.getTileColor()) : "Gold tile must have distinct tile color";
        assert heal.getTileColor() != null && !heal.getTileColor().equals(std.getTileColor()) : "Healing tile must have distinct tile color";
        assert !gold.getTileColor().equals(heal.getTileColor()) : "Gold and Healing tile colors must be distinct";

        assert std.getBorderColor() != null : "Standard tile must have a border color";
        assert gold.getBorderColor() != null && !gold.getBorderColor().equals(std.getBorderColor()) : "Gold tile must have distinct border color";
        assert heal.getBorderColor() != null && !heal.getBorderColor().equals(std.getBorderColor()) : "Healing tile must have distinct border color";

        System.out.println("  ✓ Tile decorators (Standard, Gold, Healing) behave correctly with dynamic effects and distinct colors.");
    }
}
