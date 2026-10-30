package juanword;

import java.awt.*;

/**
 * PATTERN 4: DECORATOR - Concrete Decorator 2
 * Healing Tile Decorator: Heals Juan by +8 HP when successfully submitted
 * in a valid word, and renders a Forest Green heart badge.
 */
public class HealingTileDecorator extends TileDecorator {

    public HealingTileDecorator(TileComponent wrapped) {
        super(wrapped);
    }

    @Override
    public String getEffectName() {
        return "HEAL";
    }

    @Override
    public String getEffectDescription() {
        return "💚 +8 HP";
    }

    @Override
    public void onWordSubmitted(Player player, BattlePanel panel) {
        super.onWordSubmitted(player, panel);
        // Decorator heals Juan!
        if (panel != null) {
            panel.healJuan(8);
        }
    }

    @Override
    public void renderDecoratorBadge(Graphics2D g2, int x, int y, int w, int h) {
        super.renderDecoratorBadge(g2, x, y, w, h);

        // Render small Forest Green heart badge on top-left of tile
        g2.setColor(GamePalette.FOREST_GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("♥", x + 3, y + 10);
    }
}
