package juanword;

import java.awt.*;

/**
 * PATTERN 2: FACTORY METHOD - Concrete Product 3
 * Rola - The Boss Box (Stage 3).
 */
public class RolaBossEnemy extends Enemy {

    public RolaBossEnemy() {
        super("ROLA", "ROLA (The Boss)", "Ang Hari nga Kahon (Boss)", 3, 125, 16);
    }

    @Override
    public void render(Graphics2D g2, int x, int y, int w, int h, boolean isHurt, int lungeOffset) {
        // Golden Crown on top of crate
        int crownW = 48;
        int crownH = 16;
        int crownX = x + (w - crownW) / 2;
        int crownY = y - 14;

        int[] cx = {crownX, crownX + 4, crownX + 12, crownX + (crownW / 2), crownX + crownW - 12, crownX + crownW - 4, crownX + crownW};
        int[] cy = {crownY + crownH, crownY + 2, crownY + 8, crownY, crownY + 8, crownY + 2, crownY + crownH};
        g2.setColor(GamePalette.HONEY_GOLD);
        g2.fillPolygon(cx, cy, cx.length);
        g2.setColor(GamePalette.SPICY_SWEETCORN);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawPolygon(cx, cy, cx.length);
        // Crown jewels
        g2.setColor(GamePalette.CRIMSON_RED);
        g2.fillOval(crownX + (crownW / 2) - 3, crownY + 6, 6, 6);

        // Body: Solid Crimson Red
        g2.setColor(isHurt ? GamePalette.DARK_RED : GamePalette.CRIMSON_RED);
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // Royal Dark Red border
        g2.setColor(GamePalette.DARK_RED);
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // Spiked / Studded Corners: Spicy Sweetcorn
        g2.setColor(GamePalette.SPICY_SWEETCORN);
        g2.fillOval(x + 4, y + 4, 6, 6);
        g2.fillOval(x + w - 10, y + 4, 6, 6);
        g2.fillOval(x + 4, y + h - 10, 6, 6);
        g2.fillOval(x + w - 10, y + h - 10, 6, 6);

        // Expressive Boss Eyes: Amber glowing menacing eyes
        if (isHurt) {
            g2.setColor(GamePalette.HONEY_GOLD);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(">  <", x + (w / 2) - 14, y + 24);
        } else {
            g2.setColor(GamePalette.HONEY_GOLD);
            g2.fillOval(x + 26, y + 14, 15, 14);
            g2.fillOval(x + 68, y + 14, 15, 14);
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.fillOval(x + 31, y + 16, 6, 10);
            g2.fillOval(x + 72, y + 16, 6, 10);
            // Angry angular brows
            g2.setColor(GamePalette.DARK_RED);
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(x + 24, y + 10, x + 43, y + 15);
            g2.drawLine(x + 85, y + 10, x + 66, y + 15);
        }

        // Nameplate Plaque: ROLA (BOSS)
        int plateW = 90;
        int plateH = 22;
        int plateX = x + (w - plateW) / 2;
        int plateY = y + 44;

        g2.setColor(GamePalette.HONEY_GOLD);
        g2.fillRoundRect(plateX, plateY, plateW, plateH, 6, 6);
        g2.setColor(GamePalette.SPICY_SWEETCORN);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(plateX, plateY, plateW, plateH, 6, 6);

        g2.setColor(GamePalette.MIDNIGHT_NAVY);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        String nameStr = "👑 ROLA (BOSS)";
        int tw = g2.getFontMetrics().stringWidth(nameStr);
        g2.drawString(nameStr, plateX + (plateW - tw) / 2, plateY + 16);
    }
}
