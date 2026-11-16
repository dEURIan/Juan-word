import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import juanword.model.AttackType;
import juanword.ui.AttackEffect;
import juanword.ui.ImageLoader;
import juanword.ui.PlayerAnimator;

public class TestJuanAttackAnimation {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: JUAN CHARACTER ANIMATOR & ATTACK EFFECT ===");

        BufferedImage idleSprite = ImageLoader.load("maincharacteridle.png");
        BufferedImage preAttackSprite = ImageLoader.load("maincharapreattack.png");
        BufferedImage attackSprite = ImageLoader.load("maincharattack.png");

        assert idleSprite != null : "Juan idle sprite must load";
        assert preAttackSprite != null : "Juan pre-attack sprite must load";
        assert attackSprite != null : "Juan attack sprite must load";

        PlayerAnimator animator = new PlayerAnimator(80, 210);

        // 1. Initial State: Idle
        assert !animator.isAttacking() : "Animator must initially not be attacking";
        assert !animator.consumeImpact() : "Impact must not be triggered initially";
        assert animator.getIdleElapsedMs() == 0 : "Idle time should start at 0";
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == idleSprite : "Must return idle sprite initially";

        // Idle time progression
        animator.update(250);
        assert animator.getIdleElapsedMs() == 250 : "Idle time must accumulate";
        assert animator.getDrawOffsetY() != 0.0f : "Idle breathing should produce vertical offset";
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == idleSprite : "Must remain idle sprite while idle";

        // 2. Start Attack (SLASH)
        animator.startAttack(AttackType.SLASH);
        assert animator.isAttacking() : "Animator must be attacking";

        // Phase 1: Pre-Attack (Anticipation / Wind-up, 0 - 35% of 360ms = 0 - 126ms)
        animator.update(60); // 60ms (~16.6%)
        assert animator.isAttacking() : "Animator still attacking";
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == preAttackSprite :
            "Phase 1 must return pre-attack sprite";
        assert animator.getDrawOffsetX() < 0.0f : "Phase 1 must pull back (negative X offset for wind-up)";
        assert !animator.consumeImpact() : "Impact must not be triggered during pre-attack";

        // Phase 2: Attack Strike & Lunge (35% - 75% = 126ms - 270ms)
        animator.update(90); // total 150ms (~41.6%)
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == attackSprite :
            "Phase 2 must return attack sprite";
        assert animator.getDrawOffsetX() > 0.0f : "Phase 2 must lunge forward (positive X offset for strike)";

        // Impact reached at midpoint (360ms / 2 = 180ms)
        animator.update(40); // total 190ms (past 180ms)
        assert animator.consumeImpact() : "Impact must be reached and consumed";
        assert !animator.consumeImpact() : "Impact must only be consumed once";
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == attackSprite :
            "Impact frame must still use attack sprite";

        // Phase 3: Recovery (75% - 100% = 270ms - 360ms)
        animator.update(100); // total 290ms (~80.5%)
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == idleSprite :
            "Phase 3 recovery must return idle sprite";

        // Completion (> 360ms)
        animator.update(80); // total 370ms (> 360ms)
        assert !animator.isAttacking() : "Attack animation must complete";
        assert animator.getActiveSprite(idleSprite, preAttackSprite, attackSprite) == idleSprite :
            "Post-attack must return idle sprite";

        // 3. Hit Flash & Shake
        animator.triggerHit();
        BufferedImage testCanvas = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = testCanvas.createGraphics();
        animator.paint(g2, idleSprite, 50, 50, 100, 150);
        g2.dispose();

        // 4. Attack Effect
        AttackEffect effect = new AttackEffect(AttackType.SLASH, 150, 150);
        assert !effect.isFinished() : "Attack effect must start active";
        effect.update(150);
        assert !effect.isFinished() : "Attack effect not finished at half time";
        effect.update(200);
        assert effect.isFinished() : "Attack effect must finish after duration";

        System.out.println("  ✓ PlayerAnimator idle and 3-phase attack animation verified successfully.");
        System.out.println("=== ALL JUAN ATTACK ANIMATION TESTS PASSED! ===");
        System.exit(0);
    }
}
