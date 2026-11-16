package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import juanword.model.AttackType;

public final class AttackEffect {
    private final AttackType type;
    private final int centerX;
    private final int centerY;
    private long elapsedMs;
    private final long durationMs;
    private boolean finished;

    public AttackEffect(AttackType type, int centerX, int centerY) {
        this.type = type != null ? type : AttackType.SLASH;
        this.centerX = centerX;
        this.centerY = centerY;
        this.elapsedMs = 0;
        this.durationMs = 300;
        this.finished = false;
    }

    public void update(long dtMs) {
        elapsedMs += dtMs;
        if (elapsedMs >= durationMs) {
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void paint(Graphics2D g2) {
        if (finished) return;
        float progress = Math.min(1.0f, (float) elapsedMs / durationMs);
        float alpha = Math.max(0.0f, 1.0f - progress);

        Composite oldComp = g2.getComposite();
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (type == AttackType.SLASH) {
            int len = (int) (60 + progress * 60);
            int x1 = centerX - len / 2;
            int y1 = centerY - len / 2;
            int x2 = centerX + len / 2;
            int y2 = centerY + len / 2;
            g2.setStroke(new BasicStroke(6f * (1.0f - progress * 0.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 255, (int) (alpha * 255)));
            g2.drawLine(x1, y1, x2, y2);
            g2.setColor(new Color(100, 200, 255, (int) (alpha * 180)));
            g2.drawLine(x1 - 4, y1, x2 - 4, y2);
        } else {
            // PUNCH burst / ring
            int radius = (int) (15 + progress * 55);
            g2.setStroke(new BasicStroke(4f * (1.0f - progress * 0.6f)));
            g2.setColor(new Color(255, 220, 100, (int) (alpha * 255)));
            g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3;
                int r1 = radius - 8;
                int r2 = radius + 10;
                int sx = (int) (centerX + r1 * Math.cos(angle));
                int sy = (int) (centerY + r1 * Math.sin(angle));
                int ex = (int) (centerX + r2 * Math.cos(angle));
                int ey = (int) (centerY + r2 * Math.sin(angle));
                g2.drawLine(sx, sy, ex, ey);
            }
        }

        g2.setComposite(oldComp);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint != null ? oldHint : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
}
