package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import juanword.model.AttackType;

public final class PlayerAnimator {
    private final int anchorX;
    private final int anchorY;
    private AttackType attackType;
    private long attackElapsedMs;
    private long attackDurationMs;
    private boolean impactReached;
    private int flashRemainingMs;
    private float shakeAmplitude;
    private float shakePhase;

    public PlayerAnimator(int anchorX, int anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public void startAttack(AttackType type) {
        this.attackType = (type != null) ? type : AttackType.SLASH;
        this.attackDurationMs = 360;
        this.attackElapsedMs = 0;
        this.impactReached = false;
    }

    public boolean isAttacking() {
        return attackType != null;
    }

    public BufferedImage getActiveSprite(BufferedImage idle, BufferedImage preAttack, BufferedImage attack) {
        if (attackType == null) return idle;
        float t = (float) attackElapsedMs / attackDurationMs;
        if (t < 0.35f) {
            return (preAttack != null) ? preAttack : idle;
        } else if (t < 0.75f) {
            return (attack != null) ? attack : idle;
        } else {
            return idle;
        }
    }

    public boolean consumeImpact() {
        if (impactReached) {
            impactReached = false;
            return true;
        }
        return false;
    }

    public void triggerHit() {
        this.flashRemainingMs = 120;
        this.shakeAmplitude = 14.0f;
        this.shakePhase = 0.0f;
    }

    public void update(long dtMs) {
        if (attackType != null) {
            long prev = attackElapsedMs;
            attackElapsedMs += dtMs;
            if (prev < attackDurationMs / 2 && attackElapsedMs >= attackDurationMs / 2) {
                impactReached = true;
            }
            if (attackElapsedMs >= attackDurationMs) {
                attackType = null;
            }
        }

        if (flashRemainingMs > 0) {
            flashRemainingMs = (int) Math.max(0, flashRemainingMs - dtMs);
        }

        if (shakeAmplitude > 0) {
            shakePhase += 0.04f * dtMs;
            shakeAmplitude = Math.max(0.0f, shakeAmplitude - 0.04f * dtMs);
        }
    }

    public void paint(Graphics2D g2, BufferedImage sprite) {
        if (sprite == null) return;
        paint(g2, sprite, anchorX, anchorY, sprite.getWidth(), sprite.getHeight());
    }

    public void paint(Graphics2D g2, BufferedImage sprite, int targetX, int targetY, int targetW, int targetH) {
        if (sprite == null) return;

        AffineTransform oldXf = g2.getTransform();
        float shakeOffset = (shakeAmplitude > 0) ? (float) (Math.sin(shakePhase) * shakeAmplitude) : 0f;

        double rotation = 0.0;
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (attackType != null) {
            float t = (float) attackElapsedMs / attackDurationMs;
            if (t < 0.35f) {
                // Wind-up: lean back
                float p = t / 0.35f;
                rotation = -0.15 * p;
            } else if (t < 0.70f) {
                // Swing: lean forward sharply
                float p = (t - 0.35f) / 0.35f;
                rotation = -0.15 + 0.35 * p;
                scaleX = 1.08;
            } else {
                // Recover
                float p = (t - 0.70f) / 0.30f;
                rotation = 0.20 * (1.0 - p);
            }
        }

        int drawX = (int) (targetX + shakeOffset);
        int drawY = targetY;

        g2.translate(drawX + targetW / 2.0, drawY + targetH / 2.0);
        if (rotation != 0.0) g2.rotate(rotation);
        if (scaleX != 1.0 || scaleY != 1.0) g2.scale(scaleX, scaleY);
        g2.translate(-targetW / 2.0, -targetH / 2.0);

        g2.drawImage(sprite, 0, 0, targetW, targetH, null);

        if (flashRemainingMs > 0) {
            BufferedImage flashMask = new BufferedImage(Math.max(1, targetW), Math.max(1, targetH), BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = flashMask.createGraphics();
            fg.drawImage(sprite, 0, 0, targetW, targetH, null);
            fg.setComposite(AlphaComposite.SrcIn);
            fg.setColor(new Color(255, 255, 255, 200));
            fg.fillRect(0, 0, targetW, targetH);
            fg.dispose();
            g2.drawImage(flashMask, 0, 0, null);
        }

        g2.setTransform(oldXf);
    }
}
