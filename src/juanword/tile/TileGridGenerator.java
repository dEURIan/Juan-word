package juanword.tile;

import java.util.List;
import java.util.Random;
import juanword.data.DataManager;

public final class TileGridGenerator {
    private static final Random RNG = new Random();

    private TileGridGenerator() {}

    public static TileComponent[][] generateFresh4x4() {
        TileComponent[][] grid = new TileComponent[4][4];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                grid[r][c] = generateTile();
            }
        }
        return grid;
    }

    public static TileComponent generateTile() { //odds for tiles
        char letter = DataManager.getInstance().drawRandomLetter();
        TileComponent base = new StandardTileComponent(letter);
        int roll = RNG.nextInt(100);
        if (roll < 78) {
            return base;
        } else if (roll < 90) { 
            return new GoldTileDecorator(base);
        } else {
            return new HealingTileDecorator(base);
        }
    }

    public static TileComponent applyModifier(TileComponent base, String code) {
        if (base == null) return null;
        if (code == null) return base;
        String trimmed = code.trim().toUpperCase();
        if ("GLD".equals(trimmed)) {
            return new GoldTileDecorator(base);
        } else if ("HEL".equals(trimmed)) {
            return new HealingTileDecorator(base);
        }
        return base;
    }

    public static void refill(TileComponent[][] grid, List<int[]> positions) {
        if (grid == null || positions == null) return;
        for (int[] pos : positions) {
            if (pos != null && pos.length >= 2) {
                int r = pos[0];
                int c = pos[1];
                if (r >= 0 && r < grid.length && c >= 0 && c < grid[r].length) {
                    grid[r][c] = generateTile();
                }
            }
        }
    }
}
