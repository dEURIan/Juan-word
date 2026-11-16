package juanword.tile;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import juanword.model.BattleContext;

public final class GoldTileDecorator extends TileDecorator {
    private static final Color HONEY_GOLD = new Color(255, 193, 7);
    private static final Color TILE_GOLD_BG = new Color(255, 218, 90);
    private static final Color TILE_GOLD_BORDER = new Color(225, 155, 15);

    public GoldTileDecorator(TileComponent wrapped) {
        super(wrapped);
    }

    @Override
    public int getPoints() {
        return wrapped.getPoints() * 2;
    }

    @Override
    public String getModifierCode() {
        return "GLD";
    }

    @Override
    public void onWordSubmitted(BattleContext ctx) {
        super.onWordSubmitted(ctx);
        if (ctx != null && ctx.getPlayer() != null) {
            ctx.getPlayer().addScore(getPoints() * 15);
        }
    }

    @Override
    public Color getTileColor() {
        return TILE_GOLD_BG;
    }

    @Override
    public Color getBorderColor() {
        return TILE_GOLD_BORDER;
    }

    @Override
    public void renderTileOverlay(Graphics2D g, int x, int y, int size) {
        super.renderTileOverlay(g, x, y, size);

        // Luminous warm golden wash over sprite
        Graphics2D gTint = (Graphics2D) g.create();
        gTint.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.32f));
        gTint.setColor(new Color(255, 195, 25));
        gTint.fillRoundRect(x, y, size, size, 8, 8);
        gTint.dispose();

        // Shimmering gold metallic border
        g.setColor(new Color(230, 160, 15, 220));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(x, y, size, size, 8, 8);
    }

    @Override
    public void renderBadge(Graphics2D g, int x, int y, int size) {
        super.renderBadge(g, x, y, size);
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw small gold star badge at (x + 10, y + 12) with shadow outline
        int cx = x + 10;
        int cy = y + 12;
        int outerR = 6;
        int innerR = 3;
        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? outerR : innerR;
            star.addPoint((int) (cx + r * Math.cos(angle)), (int) (cy + r * Math.sin(angle)));
        }
        g.setColor(new Color(60, 35, 5, 160));
        g.setStroke(new BasicStroke(1.2f));
        g.drawPolygon(star);
        g.setColor(HONEY_GOLD);
        g.fillPolygon(star);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint != null ? oldHint : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
}
