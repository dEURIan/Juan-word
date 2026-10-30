package juanword;

import java.awt.Graphics2D;

/**
 * PATTERN 4: DECORATOR - sir gudoooooo
 * 
 */
public abstract class TileDecorator implements TileComponent {
    protected final TileComponent wrapped;

    public TileDecorator(TileComponent wrapped) {
        this.wrapped = wrapped;
    }

    public TileComponent getWrapped() {
        return wrapped;
    }

    @Override
    public char getLetter() {
        return wrapped.getLetter();
    }

    @Override
    public int getBasePoints() {
        return wrapped.getBasePoints();
    }

    @Override
    public int getPoints() {
        return wrapped.getPoints();
    }

    @Override
    public String getEffectName() {
        return wrapped.getEffectName();
    }

    @Override
    public String getEffectDescription() {
        return wrapped.getEffectDescription();
    }

    @Override
    public void onWordSubmitted(Player player, BattlePanel panel) {
        wrapped.onWordSubmitted(player, panel);
    }

    @Override
    public void renderDecoratorBadge(Graphics2D g2, int x, int y, int w, int h) {
        wrapped.renderDecoratorBadge(g2, x, y, w, h);
    }
}
