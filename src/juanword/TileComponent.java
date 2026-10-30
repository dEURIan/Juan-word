package juanword;

import java.awt.Graphics2D;

/**
 * PATTERN 4: DECORATOR - Component Interface
 * Defines the contract for letter tiles and decorated bonus tiles in JuanWord.
 */
public interface TileComponent {
    char getLetter();
    int getBasePoints();
    int getPoints();
    String getEffectName();
    String getEffectDescription();
    void onWordSubmitted(Player player, BattlePanel panel);
    void renderDecoratorBadge(Graphics2D g2, int x, int y, int w, int h);
}
