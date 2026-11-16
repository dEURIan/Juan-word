package scratch;

import juanword.JuanWord;
import juanword.model.*;
import juanword.ui.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TestEnemyDamagePreviewAndDeltaTime {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Running TestEnemyDamagePreviewAndDeltaTime ---");

        testHpPreviewCalculations();
        testDamageSliceWidth();
        testAnimatorDeltaTimeProgression();
        testBattlePanelDamagePreviewIntegration();
        testBattlePanelFullscreenRenderingAndCache();

        System.out.println("ALL CHECKS PASSED: Enemy damage preview and fullscreen delta-time animation verified!");
        System.exit(0);
    }

    private static void testHpPreviewCalculations() {
        System.out.println("1. Testing calculatePreviewRemainingHp...");
        assert BattlePanel.calculatePreviewRemainingHp(50, 18) == 32 : "50 - 18 should be 32";
        assert BattlePanel.calculatePreviewRemainingHp(50, 0) == 50 : "50 - 0 should be 50";
        assert BattlePanel.calculatePreviewRemainingHp(50, 50) == 0 : "50 - 50 should be 0";
        assert BattlePanel.calculatePreviewRemainingHp(50, 80) == 0 : "50 - 80 should clamp to 0";
        assert BattlePanel.calculatePreviewRemainingHp(50, -10) == 50 : "Negative damage should not heal";
        assert BattlePanel.calculatePreviewRemainingHp(0, 10) == 0 : "0 hp remaining should be 0";
        System.out.println(" -> calculatePreviewRemainingHp logic verified.");
    }

    private static void testDamageSliceWidth() {
        System.out.println("2. Testing calculateDamageSliceWidth...");
        int bw = 160;
        int maxHp = 50;

        // Half damage
        int sliceHalf = BattlePanel.calculateDamageSliceWidth(bw, 50, maxHp, 25);
        assert sliceHalf == 80 : "Half damage on 160px bar should be 80px slice, got: " + sliceHalf;

        // Full lethal damage
        int sliceFull = BattlePanel.calculateDamageSliceWidth(bw, 50, maxHp, 50);
        assert sliceFull == 160 : "Full lethal damage should cover entire 160px bar, got: " + sliceFull;

        // Zero damage
        int sliceZero = BattlePanel.calculateDamageSliceWidth(bw, 50, maxHp, 0);
        assert sliceZero == 0 : "Zero damage preview should have 0px slice, got: " + sliceZero;

        // Oversized damage
        int sliceOver = BattlePanel.calculateDamageSliceWidth(bw, 50, maxHp, 100);
        assert sliceOver == 160 : "Overkill damage should clamp to current bar width, got: " + sliceOver;

        // Fullscreen 1080p bar width (320px)
        int fsWidth = 320;
        int fsSlice = BattlePanel.calculateDamageSliceWidth(fsWidth, 50, maxHp, 25);
        assert fsSlice == 160 : "Fullscreen 320px bar half damage should be 160px, got: " + fsSlice;

        System.out.println(" -> calculateDamageSliceWidth logic verified.");
    }

    private static void testAnimatorDeltaTimeProgression() {
        System.out.println("3. Testing Animator delta time progression...");
        PlayerAnimator pa = new PlayerAnimator(85, 245);
        EnemyAnimator ea = new EnemyAnimator();

        long startIdle = pa.getIdleElapsedMs();
        pa.update(33); // Simulate 30 FPS / slow fullscreen frame
        assert pa.getIdleElapsedMs() == startIdle + 33 : "PlayerAnimator should advance by exact delta time 33ms";

        pa.update(16); // Simulate 60 FPS frame
        assert pa.getIdleElapsedMs() == startIdle + 49 : "PlayerAnimator should advance by exact delta time 16ms";

        // Attack timing test with variable dt
        pa.startAttack(AttackType.SLASH);
        pa.update(180); // Half of 360ms duration
        assert pa.consumeImpact() : "Impact should be reached at halfway (180ms)";
        pa.update(180); // Complete attack
        assert pa.getDrawOffsetX() == 0.0f : "Attack should finish cleanly after duration";

        System.out.println(" -> Animator delta-time progression verified.");
    }

    private static void testBattlePanelDamagePreviewIntegration() throws Exception {
        System.out.println("4. Testing BattlePanel damage preview integration...");
        JuanWord host = new JuanWord();
        BattlePanel bp = new BattlePanel(host);

        Player p = new Player(1, "Ikaw");
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(p)
            .onMap(1)
            .atStage(1)
            .build();
        bp.begin(session);

        // Initially no tiles selected -> 0 damage preview
        assert bp.getSelectedDamagePreview() == 0 : "Initial damage preview should be 0";

        // Access selectedPath field
        Field pathField = BattlePanel.class.getDeclaredField("selectedPath");
        pathField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<int[]> path = (List<int[]>) pathField.get(bp);

        // Add 2 tiles to path
        path.add(new int[]{0, 0});
        path.add(new int[]{0, 1});

        int dmg = bp.getSelectedDamagePreview();
        assert dmg > 0 : "Damage preview should be > 0 with tiles selected, got: " + dmg;

        // Clear path -> back to 0
        bp.onClear();
        assert bp.getSelectedDamagePreview() == 0 : "Damage preview should be 0 after clear";

        System.out.println(" -> BattlePanel damage preview integration verified.");
    }

    private static void testBattlePanelFullscreenRenderingAndCache() throws Exception {
        System.out.println("5. Testing BattlePanel windowed and fullscreen rendering with background cache...");
        JuanWord host = new JuanWord();
        BattlePanel bp = new BattlePanel(host);

        Player p = new Player(1, "Ikaw");
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(p)
            .onMap(1)
            .atStage(1)
            .build();
        bp.begin(session);

        // Select tiles to verify damage preview rendering
        Field pathField = BattlePanel.class.getDeclaredField("selectedPath");
        pathField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<int[]> path = (List<int[]>) pathField.get(bp);
        path.add(new int[]{0, 0});
        path.add(new int[]{0, 1});
        path.add(new int[]{0, 2});

        // 1. Render Windowed (960x540)
        BufferedImage imgWin = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gWin = imgWin.createGraphics();
        bp.setSize(960, 540);
        bp.paint(gWin);
        gWin.dispose();

        // 2. Render Fullscreen (1920x1080)
        BufferedImage imgFs = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFs = imgFs.createGraphics();
        bp.setSize(1920, 1080);
        bp.paint(gFs);
        gFs.dispose();

        // 3. Render second frame fullscreen (verify cache reuse)
        BufferedImage imgFs2 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFs2 = imgFs2.createGraphics();
        bp.paint(gFs2);
        gFs2.dispose();

        Field cachedBgField = BattlePanel.class.getDeclaredField("cachedBgImage");
        cachedBgField.setAccessible(true);
        BufferedImage cached = (BufferedImage) cachedBgField.get(bp);
        assert cached != null : "Cached background should not be null";
        assert cached.getWidth() == 1920 && cached.getHeight() == 1080 : "Cached background should be 1920x1080";

        System.out.println(" -> Fullscreen rendering and background caching verified.");
    }
}
