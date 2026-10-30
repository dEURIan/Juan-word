package juanword;

import java.awt.*;

/**
 * PATTERN 4: DECORATOR - Concrete Decorator 1
 * Gold Tile Decorator: Doubles the point value of the letter and renders
 * a shimmering Honey Gold star badge.
 */
public class GoldTileDecorator extends TileDecorator {

    public GoldTileDecorator(TileComponent wrapped) {
        super(wrapped);
    }

    @Override
    public int getPoints() {
        // Decorator modifies behavior: 2x points!
        return wrapped.getPoints() * 2;
    }

    @Override
    public String getEffectName() {
        return "GOLD";
    }

    @Override
    public String getEffectDescription() {
        return "⭐ 2x Puntos";
    }

    @Override
    public void onWordSubmitted(Player player, BattlePanel panel) {
        super.onWordSubmitted(player, panel);
        // Bonus score feedback
        if (player != null) {
            player.score += getPoints();
        }
    }

    @Override
    public void renderDecoratorBadge(Graphics2D g2, int x, int y, int w, int h) {
        super.renderDecoratorBadge(g2, x, y, w, h);

        // Render small Honey Gold star badge on top-left of tile
        g2.setColor(GamePalette.HONEY_GOLD);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("★", x + 3, y + 10);
    }
}
