import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import juanword.model.AttackType;
import juanword.ui.AttackEffect;
import juanword.ui.FloatingText;
import juanword.ui.ImageLoader;
import juanword.ui.PlayerAnimator;

public class TestJuanAttackAnimation {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: JUAN CHARACTER ANIMATOR & ATTACK EFFECT ===");

        BufferedImage juanSprite = ImageLoader.load("maincharacteridle.png");
        assert juanSprite != null : "Juan sprite must load";

        PlayerAnimator animator = new PlayerAnimator(80, 210);

        // 1. Initial State
        assert !animator.isAttacking() : "Animator must initially not be attacking";
        assert !animator.consumeImpact() : "Impact must not be triggered initially";

        // 2. Start Attack (SLASH)
        animator.startAttack(AttackType.SLASH);
        assert animator.isAttacking() : "Animator must be attacking";

        // Update before midpoint
        animator.update(100);
        assert animator.isAttacking() : "Animator still attacking";
        assert !animator.consumeImpact() : "Impact not reached yet";

        // Update past impact frame (360ms / 2 = 180ms)
        animator.update(100); // 200ms total
        assert animator.consumeImpact() : "Impact must be reached and consumed";
        assert !animator.consumeImpact() : "Impact must only be consumed once";

        // Update to completion
        animator.update(200);
        assert !animator.isAttacking() : "Attack animation must complete";

        // 3. Hit Flash & Shake
        animator.triggerHit();
        BufferedImage testCanvas = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = testCanvas.createGraphics();
        animator.paint(g2, juanSprite);
        g2.dispose();

        // 4. Attack Effect
        AttackEffect effect = new AttackEffect(AttackType.SLASH, 150, 150);
        assert !effect.isFinished() : "Attack effect must start active";
        effect.update(150);
        assert !effect.isFinished() : "Attack effect not finished at half time";
        effect.update(200);
        assert effect.isFinished() : "Attack effect must finish after duration";

        System.out.println("  ✓ PlayerAnimator and AttackEffect verified successfully according to Flowchart 14.");
        System.out.println("=== ALL JUAN ATTACK ANIMATION TESTS PASSED! ===");
        System.exit(0);
    }
}
