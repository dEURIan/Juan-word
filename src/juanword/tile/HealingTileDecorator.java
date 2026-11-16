package juanword.tile;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import juanword.model.BattleContext;

public final class HealingTileDecorator extends TileDecorator {
    private static final Color FOREST_GREEN = new Color(46, 125, 50);
    private static final Color TILE_HEALING_BG = new Color(185, 238, 202);
    private static final Color TILE_HEALING_BORDER = new Color(36, 148, 72);

    public HealingTileDecorator(TileComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getModifierCode() {
        return "HEL";
    }

    @Override
    public void onWordSubmitted(BattleContext ctx) {
        super.onWordSubmitted(ctx);
        if (ctx != null) {
            ctx.healPlayer(8);
        }
    }

    @Override
    public Color getTileColor() {
        return TILE_HEALING_BG;
    }

    @Override
    public Color getBorderColor() {
        return TILE_HEALING_BORDER;
    }

    @Override
    public void renderTileOverlay(Graphics2D g, int x, int y, int size) {
        super.renderTileOverlay(g, x, y, size);

        // Soothing, radiant emerald wash over sprite
        Graphics2D gTint = (Graphics2D) g.create();
        gTint.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.30f));
        gTint.setColor(new Color(40, 195, 90));
        gTint.fillRoundRect(x, y, size, size, 8, 8);
        gTint.dispose();

        // Vibrant healing emerald border
        g.setColor(new Color(36, 160, 75, 220));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(x, y, size, size, 8, 8);
    }

    @Override
    public void renderBadge(Graphics2D g, int x, int y, int size) {
        super.renderBadge(g, x, y, size);
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw small forest-green heart badge in top-left with subtle shadow
        int hx = x + 6;
        int hy = y + 7;
        g.setColor(new Color(15, 60, 25, 140));
        g.setStroke(new BasicStroke(1.2f));
        g.drawArc(hx - 1, hy - 1, 6, 6, 0, 180);
        g.drawArc(hx + 3, hy - 1, 6, 6, 0, 180);

        g.setColor(FOREST_GREEN);
        g.fillArc(hx, hy, 5, 5, 0, 180);
        g.fillArc(hx + 4, hy, 5, 5, 0, 180);
        int[] px = {hx, hx + 9, hx + 4};
        int[] py = {hy + 3, hy + 3, hy + 9};
        g.fillPolygon(px, py, 3);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint != null ? oldHint : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
}
