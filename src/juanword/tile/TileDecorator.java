package juanword.tile;

import java.awt.Color;
import java.awt.Graphics2D;
import juanword.model.BattleContext;

public abstract class TileDecorator implements TileComponent {
    protected final TileComponent wrapped;

    protected TileDecorator(TileComponent wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped tile cannot be null");
        }
        this.wrapped = wrapped;
    }

    @Override
    public char getLetter() {
        return wrapped.getLetter();
    }

    @Override
    public int getPoints() {
        return wrapped.getPoints();
    }

    @Override
    public String getModifierCode() {
        return wrapped.getModifierCode();
    }

    @Override
    public void onWordSubmitted(BattleContext ctx) {
        wrapped.onWordSubmitted(ctx);
    }

    @Override
    public void renderBadge(Graphics2D g, int x, int y, int size) {
        wrapped.renderBadge(g, x, y, size);
    }

    @Override
    public Color getTileColor() {
        return wrapped.getTileColor();
    }

    @Override
    public Color getBorderColor() {
        return wrapped.getBorderColor();
    }

    @Override
    public void renderTileOverlay(Graphics2D g, int x, int y, int size) {
        wrapped.renderTileOverlay(g, x, y, size);
    }
}
