package juanword;

import java.awt.Graphics2D;

/**
 * PATTERN 4: DECORATOR - Concrete Component
 * Standard plain letter tile without special modifiers.
 */
public class StandardTileComponent implements TileComponent {
    private final char letter;
    private final int points;

    public StandardTileComponent(char letter, int points) {
        this.letter = Character.toLowerCase(letter);
        this.points = points;
    }

    @Override
    public char getLetter() {
        return letter;
    }

    @Override
    public int getBasePoints() {
        return points;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public String getEffectName() {
        return "STANDARD";
    }

    @Override
    public String getEffectDescription() {
        return "";
    }

    @Override
    public void onWordSubmitted(Player player, BattlePanel panel) {
        // Standard tiles have no additional trigger
    }

    @Override
    public void renderDecoratorBadge(Graphics2D g2, int x, int y, int w, int h) {
        // No special badge for standard tiles
    }
}
