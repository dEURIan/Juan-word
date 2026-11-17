package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import juanword.model.AttackType;

public final class EnemyAnimator {
    private float x;
    private float targetX;
    private long entranceElapsedMs = 600;
    private int flashRemainingMs;
    private float shakeAmplitude;
    private float shakePhase;
    private AttackType attackType;
    private long attackElapsedMs;
    private long attackDurationMs;
    private boolean impactReached;
    private long idleElapsedMs;

    public void startEntrance(int targetX) {
        this.targetX = targetX;
        this.x = 960.0f;
        this.entranceElapsedMs = 0;
    }

    public void startAttack(AttackType type) {
        startAttack(type, 360);
    }

    public void startAttack(AttackType type, long durationMs) {
        this.attackType = (type != null) ? type : AttackType.PUNCH;
        this.attackDurationMs = Math.max(100, durationMs);
        this.attackElapsedMs = 0;
        this.impactReached = false;
    }

    public long getAttackDurationMs() {
        return attackDurationMs;
    }

    public long getAttackElapsedMs() {
        return attackElapsedMs;
    }

    public boolean isAttacking() {
        return attackType != null;
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
        if (entranceElapsedMs < 600) {
            entranceElapsedMs += dtMs;
            float t = Math.min(1.0f, (float) entranceElapsedMs / 600.0f);
            float ease = 1.0f - (float) Math.pow(1.0f - t, 3); // cubic ease-out
            this.x = 960.0f + (targetX - 960.0f) * ease;
        } else {
            this.x = targetX;
        }

        if (attackType != null) {
            long prev = attackElapsedMs;
            attackElapsedMs += dtMs;
            if (prev < attackDurationMs / 2 && attackElapsedMs >= attackDurationMs / 2) {
                impactReached = true;
            }
            if (attackElapsedMs >= attackDurationMs) {
                attackType = null;
            }
        } else {
            idleElapsedMs += dtMs;
        }

        if (flashRemainingMs > 0) {
            flashRemainingMs = (int) Math.max(0, flashRemainingMs - dtMs);
        }

        if (shakeAmplitude > 0) {
            shakePhase += 0.04f * dtMs;
            shakeAmplitude = Math.max(0.0f, shakeAmplitude - 0.04f * dtMs);
        }
    }

    public BufferedImage getActiveSprite(BufferedImage idle, BufferedImage attack) {
        if (attackType == null || attack == null) return idle;
        float t = (float) attackElapsedMs / attackDurationMs;
        if (t >= 0.30f && t < 0.78f) {
            return attack;
        }
        return idle;
    }

    public float getDrawOffsetX() {
        float lungeX = 0f;
        if (attackType != null) {
            float t = (float) attackElapsedMs / attackDurationMs;
            if (t < 0.30f) {
                float p = t / 0.30f;
                // Pull back to the right (away from player on the left)
                lungeX = (float) (12.0 * Math.sin(p * Math.PI * 0.5));
            } else if (t < 0.78f) {
                float p = (t - 0.30f) / 0.48f;
                // Lunge forward to the left (towards player on the left)
                lungeX = (float) (-38.0 * Math.sin(p * Math.PI));
            } else {
                float p = (t - 0.78f) / 0.22f;
                lungeX = (float) (-12.0 * (1.0 - p));
            }
        }
        float shakeOffset = (shakeAmplitude > 0) ? (float) (Math.sin(shakePhase) * shakeAmplitude) : 0f;
        return lungeX + shakeOffset;
    }

    public float getDrawOffsetY() {
        if (attackType == null) {
            return (float) (Math.sin(idleElapsedMs * 0.0035) * 2.5);
        }
        return 0f;
    }

    public float getShadowScale() {
        if (attackType == null) {
            return 1.0f - 0.04f * (float) Math.sin(idleElapsedMs * 0.0035);
        }
        float t = (float) attackElapsedMs / attackDurationMs;
        if (t >= 0.30f && t < 0.78f) {
            float p = (t - 0.30f) / 0.48f;
            return 1.0f + 0.12f * (float) Math.sin(p * Math.PI);
        }
        return 1.0f;
    }

    public long getIdleElapsedMs() {
        return idleElapsedMs;
    }

    public boolean isEntering() {
        return entranceElapsedMs < 600;
    }

    public void completeEntrance() {
        this.entranceElapsedMs = 600;
        this.x = targetX;
    }

    public void paint(Graphics2D g2, BufferedImage sprite, int y) {
        if (sprite == null) return;
        paint(g2, sprite, (int) x, y, sprite.getWidth(), sprite.getHeight());
    }

    public void paint(Graphics2D g2, BufferedImage sprite, int targetX, int targetY, int targetW, int targetH) {
        paint(g2, sprite, targetX, targetY, targetW, targetH, 0.90);
    }

    public void paint(Graphics2D g2, BufferedImage sprite, int targetX, int targetY, int targetW, int targetH, double footRatio) {
        if (sprite == null) return;

        AffineTransform oldXf = g2.getTransform();
        float offsetX = getDrawOffsetX();
        float offsetY = getDrawOffsetY();

        double rotation = 0.0;
        double scaleX = 1.0;
        double scaleY = 1.0;

        if (attackType != null) {
            float t = (float) attackElapsedMs / attackDurationMs;
            if (t < 0.30f) {
                // Wind-up: pull back and lean back (clockwise tilt)
                float p = t / 0.30f;
                rotation = 0.14 * p;
                scaleX = 0.98;
                scaleY = 1.02;
            } else if (t < 0.78f) {
                // Strike: lean forward into strike (counter-clockwise tilt)
                float p = (t - 0.30f) / 0.48f;
                rotation = 0.14 - 0.30 * Math.sin(p * Math.PI);
                scaleX = 1.08;
                scaleY = 0.96;
            } else {
                // Recover
                float p = (t - 0.78f) / 0.22f;
                rotation = -0.12 * (1.0 - p);
                scaleX = 1.0 + 0.03 * (1.0 - p);
            }
        } else {
            // Idle breathing subtle scale
            scaleY = 1.0 + 0.015 * Math.sin(idleElapsedMs * 0.0035);
            scaleX = 1.0 - 0.008 * Math.sin(idleElapsedMs * 0.0035);
        }

        int baseX = isEntering() ? (int) x : targetX;
        int drawX = (int) (baseX + offsetX);
        int drawY = (int) (targetY + offsetY);

        // Pivot around feet / lower center for grounded, natural rotation
        double pivotX = drawX + targetW / 2.0;
        double pivotRatio = (footRatio > 0.0 && footRatio <= 1.0) ? footRatio : 0.90;
        double pivotY = drawY + targetH * pivotRatio;

        g2.translate(pivotX, pivotY);
        if (rotation != 0.0) g2.rotate(rotation);
        if (scaleX != 1.0 || scaleY != 1.0) g2.scale(scaleX, scaleY);
        g2.translate(-targetW / 2.0, -targetH * pivotRatio);

        BufferedImage fitted = ImageLoader.scaledCached(sprite, Math.max(1, targetW), Math.max(1, targetH));
        g2.drawImage(fitted, 0, 0, null);

        if (flashRemainingMs > 0) {
            BufferedImage flashMask = new BufferedImage(Math.max(1, targetW), Math.max(1, targetH), BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = flashMask.createGraphics();
            fg.drawImage(fitted, 0, 0, null);
            fg.setComposite(AlphaComposite.SrcIn);
            fg.setColor(new Color(255, 255, 255, 200));
            fg.fillRect(0, 0, targetW, targetH);
            fg.dispose();
            g2.drawImage(flashMask, 0, 0, null);
        }

        g2.setTransform(oldXf);
    }
}
