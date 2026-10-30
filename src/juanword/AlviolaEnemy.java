package juanword;

import java.awt.*;

/**
 * PATTERN 2: FACTORY METHOD - Concrete Product 2
 * Alviola - The Steel Box (Stage 2).
 */
public class AlviolaEnemy extends Enemy {

    public AlviolaEnemy() {
        super("ALVIOLA", "ALVIOLA (Steel Box)", "Ang Puthaw nga Kahon", 2, 95, 13);
    }

    @Override
    public void render(Graphics2D g2, int x, int y, int w, int h, boolean isHurt, int lungeOffset) {
        // Body: Solid Cerulean (flashes Crimson Red when hurt)
        g2.setColor(isHurt ? GamePalette.CRIMSON_RED : GamePalette.CERULEAN);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Steel plate cross-brace X in Dusk Navy
        g2.setColor(GamePalette.DUSK_NAVY);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(x + 6, y + 6, x + w - 6, y + h - 6);
        g2.drawLine(x + w - 6, y + 6, x + 6, y + h - 6);

        // Steel Corner Reinforcements: Midnight Navy
        g2.setColor(GamePalette.MIDNIGHT_NAVY);
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Heavy Steel Hex Bolts: Cream
        g2.setColor(GamePalette.CREAM);
        g2.fillOval(x + 4, y + 4, 6, 6);
        g2.fillOval(x + w - 10, y + 4, 6, 6);
        g2.fillOval(x + 4, y + h - 10, 6, 6);
        g2.fillOval(x + w - 10, y + h - 10, 6, 6);

        // Expressive Eyes: Sharp, focused angular eyes
        if (isHurt) {
            g2.setColor(GamePalette.CREAM);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(">  <", x + (w / 2) - 14, y + 26);
        } else {
            g2.setColor(GamePalette.CREAM);
            g2.fillOval(x + 26, y + 15, 15, 13);
            g2.fillOval(x + 68, y + 15, 15, 13);
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.fillOval(x + 31, y + 17, 7, 7);
            g2.fillOval(x + 71, y + 17, 7, 7);
            // Fierce diagonal steel brows
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(x + 24, y + 11, x + 42, y + 16);
            g2.drawLine(x + 85, y + 11, x + 67, y + 16);
        }

        // Nameplate Plaque: ALVIOLA
        int plateW = 82;
        int plateH = 22;
        int plateX = x + (w - plateW) / 2;
        int plateY = y + 46;

        g2.setColor(GamePalette.DUSK_NAVY);
        g2.fillRoundRect(plateX, plateY, plateW, plateH, 6, 6);
        g2.setColor(GamePalette.SKY_BLUE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(plateX, plateY, plateW, plateH, 6, 6);

        g2.setColor(GamePalette.CREAM);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String nameStr = "ALVIOLA";
        int tw = g2.getFontMetrics().stringWidth(nameStr);
        g2.drawString(nameStr, plateX + (plateW - tw) / 2, plateY + 16);
    }
}
