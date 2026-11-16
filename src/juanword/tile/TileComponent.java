package juanword.tile;

import java.awt.Color;
import java.awt.Graphics2D;
import juanword.model.BattleContext;

public interface TileComponent {
    char getLetter();
    int getPoints();
    String getModifierCode();
    void onWordSubmitted(BattleContext context);
    void renderBadge(Graphics2D g2d, int tileX, int tileY, int tileSize);
    Color getTileColor();
    Color getBorderColor();
    void renderTileOverlay(Graphics2D g2d, int tileX, int tileY, int tileSize);
}
