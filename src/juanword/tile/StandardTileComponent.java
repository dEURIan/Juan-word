package juanword.tile;

import java.awt.Color;
import java.awt.Graphics2D;
import juanword.data.DataManager;
import juanword.model.BattleContext;

public final class StandardTileComponent implements TileComponent {
    private static final Color DEFAULT_TILE_COLOR = new Color(245, 235, 215);
    private static final Color DEFAULT_BORDER_COLOR = new Color(195, 175, 140);

    private final char letter;
    private final int points;

    public StandardTileComponent(char letter) {
        this.letter = Character.toUpperCase(letter);
        this.points = DataManager.getInstance().getPointsFor(this.letter);
    }

    @Override
    public char getLetter() {
        return letter;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public String getModifierCode() {
        return "STD";
    }

    @Override
    public void onWordSubmitted(BattleContext ctx) {
        // Normal tile has no special combat effect
    }

    @Override
    public void renderBadge(Graphics2D g, int x, int y, int size) {
        // Standard tile renders no badge
    }

    @Override
    public Color getTileColor() {
        return DEFAULT_TILE_COLOR;
    }

    @Override
    public Color getBorderColor() {
        return DEFAULT_BORDER_COLOR;
    }

    @Override
    public void renderTileOverlay(Graphics2D g, int x, int y, int size) {
        // Standard tile requires no overlay
    }
}
