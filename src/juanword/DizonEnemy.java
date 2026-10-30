package juanword;

import java.awt.*;

/**
 * PATTERN 2: FACTORY METHOD - Concrete Product 1
 * Dizon - The Wooden Crate (Stage 1).
 */
public class DizonEnemy extends Enemy {

    public DizonEnemy() {
        super("DIZON", "DIZON (The Box)", "Ang Kahoy nga Kahon", 1, 70, 10);
    }

    @Override
    public void render(Graphics2D g2, int x, int y, int w, int h, boolean isHurt, int lungeOffset) {
        // Body: Solid Pumpkin Orange (flashes Crimson Red when hurt)
        g2.setColor(isHurt ? GamePalette.CRIMSON_RED : GamePalette.PUMPKIN_ORANGE);
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // Wood planks horizontal seams
        g2.setColor(new Color(90, 45, 10, 140));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x + 4, y + (h / 3), x + w - 4, y + (h / 3));
        g2.drawLine(x + 4, y + (2 * h / 3), x + w - 4, y + (2 * h / 3));

        // Steel Corner Reinforcements: Dusk Navy
        g2.setColor(GamePalette.DUSK_NAVY);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // Brass Rivets: Spicy Sweetcorn
        g2.setColor(GamePalette.SPICY_SWEETCORN);
        g2.fillOval(x + 4, y + 4, 5, 5);
        g2.fillOval(x + w - 9, y + 4, 5, 5);
        g2.fillOval(x + 4, y + h - 9, 5, 5);
        g2.fillOval(x + w - 9, y + h - 9, 5, 5);

        // Expressive Eyes
        if (isHurt) {
            g2.setColor(GamePalette.CREAM);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(">  <", x + (w / 2) - 14, y + 24);
        } else {
            g2.setColor(GamePalette.CREAM);
            g2.fillOval(x + 28, y + 14, 14, 14);
            g2.fillOval(x + 68, y + 14, 14, 14);
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.fillOval(x + 32, y + 17, 7, 7);
            g2.fillOval(x + 72, y + 17, 7, 7);
            // Eyebrows
            g2.setColor(GamePalette.DUSK_NAVY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 28, y + 12, x + 42, y + 14);
            g2.drawLine(x + 68, y + 14, x + 82, y + 12);
        }

        // Nameplate Plaque: DIZON
        int plateW = 76;
        int plateH = 22;
        int plateX = x + (w - plateW) / 2;
        int plateY = y + 44;

        g2.setColor(GamePalette.HONEY_GOLD);
        g2.fillRoundRect(plateX, plateY, plateW, plateH, 6, 6);
        g2.setColor(GamePalette.SPICY_SWEETCORN);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(plateX, plateY, plateW, plateH, 6, 6);

        g2.setColor(GamePalette.MIDNIGHT_NAVY);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String nameStr = "DIZON";
        int tw = g2.getFontMetrics().stringWidth(nameStr);
        g2.drawString(nameStr, plateX + (plateW - tw) / 2, plateY + 16);
    }
}
