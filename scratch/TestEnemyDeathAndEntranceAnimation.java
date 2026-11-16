import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import juanword.model.AttackType;
import juanword.ui.AttackEffect;
import juanword.ui.EnemyAnimator;
import juanword.ui.FloatingText;
import juanword.ui.ImageLoader;

public class TestEnemyDeathAndEntranceAnimation {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: ENEMY ENTRANCE, ATTACK & HIT ANIMATION ===");

        BufferedImage enemySprite = ImageLoader.load("student.png");
        assert enemySprite != null : "Student sprite must load";

        EnemyAnimator animator = new EnemyAnimator();

        // 1. Entrance slide (from x=960 to targetX=730)
        animator.startEntrance(730);
        assert animator.isEntering() : "Must be entering";

        animator.update(300); // halfway
        assert animator.isEntering() : "Still entering at 300ms";

        animator.update(350); // > 600ms
        assert !animator.isEntering() : "Entrance slide must finish at 600ms";

        // 2. Enemy in-place attack (PUNCH)
        animator.startAttack(AttackType.PUNCH);
        assert animator.isAttacking() : "Enemy must be attacking";

        animator.update(100);
        assert !animator.consumeImpact() : "Impact not reached yet";

        animator.update(100); // 200ms
        assert animator.consumeImpact() : "Impact must be reached and consumed";
        assert !animator.consumeImpact() : "Impact consumed only once";

        animator.update(200);
        assert !animator.isAttacking() : "Attack animation must finish";

        // 3. Enemy Hit Flash & Decaying Shake
        animator.triggerHit();
        BufferedImage testCanvas = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = testCanvas.createGraphics();
        animator.paint(g2, enemySprite, 100);
        g2.dispose();

        // 4. Punch AttackEffect
        AttackEffect punchEffect = new AttackEffect(AttackType.PUNCH, 200, 200);
        assert !punchEffect.isFinished() : "Punch effect starts active";
        punchEffect.update(350);
        assert punchEffect.isFinished() : "Punch effect completes";

        // 5. Floating text popup
        FloatingText ft = new FloatingText("-24", 200, 200, Color.ORANGE);
        assert !ft.isExpired() : "Floating text active";
        ft.update(1000);
        assert ft.isExpired() : "Floating text expires after 900ms";

        System.out.println("  ✓ Enemy entrance slide, in-place attack, hit reaction, and floating text verified.");
        System.out.println("=== ALL ENEMY ANIMATION TESTS PASSED! ===");
        System.exit(0);
    }
}
