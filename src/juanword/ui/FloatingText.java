package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class FloatingText {
    private final String text;
    private final Color color;
    private float x;
    private float y;
    private final float vy;
    private int lifeMs;
    private final int maxLifeMs;

    public FloatingText(String text, int x, int y, Color color) {
        this.text = text != null ? text : "";
        this.color = color != null ? color : Color.WHITE;
        this.x = x;
        this.y = y;
        this.vy = -0.05f;
        this.lifeMs = 900;
        this.maxLifeMs = 900;
    }

    public void update(long dtMs) {
        this.y += vy * dtMs;
        this.lifeMs -= dtMs;
    }

    public boolean isExpired() {
        return lifeMs <= 0;
    }

    public void paint(Graphics2D g2) {
        if (isExpired() || text.isEmpty()) return;
        float alpha = Math.clamp((float) lifeMs / maxLifeMs, 0.0f, 1.0f);

        Composite oldComp = g2.getComposite();
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 22);
        g2.setFont(font);

        // Shadow
        g2.setColor(new Color(0, 0, 0, (int) (alpha * 180)));
        g2.drawString(text, x + 2, y + 2);

        // Foreground
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
        g2.drawString(text, x, y);

        g2.setComposite(oldComp);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldHint != null ? oldHint : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    }
}
