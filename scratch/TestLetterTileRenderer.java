import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestLetterTileRenderer {
    public static void main(String[] args) throws Exception {
        // Render a 4x4 grid simulating the battle letter tray with the new alphabet tile assets
        int gridW = 4 * 64 + 30;
        int gridH = 4 * 64 + 30;
        BufferedImage trayImg = new BufferedImage(gridW, gridH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = trayImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Dark navy tray background
        g2.setColor(new Color(20, 26, 44));
        g2.fillRoundRect(0, 0, gridW, gridH, 16, 16);
        g2.setColor(new Color(242, 190, 75));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(0, 0, gridW - 1, gridH - 1, 16, 16);

        char[] testLetters = {
            'D', 'A', 'B', 'A',
            'W', 'E', 'N', 'Y',
            'O', 'S', 'U', 'K',
            'O', 'L', 'J', 'U'
        };
        int[] testPoints = {
            2, 1, 3, 1,
            4, 1, 1, 4,
            1, 1, 1, 5,
            1, 1, 8, 1
        };
        boolean[] isSelected = {
            true, true, true, true,
            false, false, false, false,
            false, false, false, false,
            false, false, false, false
        };

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int idx = r * 4 + c;
                char ch = testLetters[idx];
                int pts = testPoints[idx];
                boolean sel = isSelected[idx];
                boolean hov = (idx == 5); // hover on 'E'

                int x = 15 + c * 64;
                int y = 15 + r * 64;
                int w = 56;
                int h = 56;

                // Load letter asset
                BufferedImage tileImg = ImageIO.read(new File("src/juanword/letters/" + ch + ".png"));
                drawTile(g2, tileImg, ch, pts, x, y, w, h, sel, hov, idx == 6, idx == 7);
            }
        }
        g2.dispose();

        // Also render Bookworm popped word rack at top
        BufferedImage fullScene = new BufferedImage(gridW, gridH + 110, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gFull = fullScene.createGraphics();
        gFull.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gFull.setColor(new Color(15, 18, 30));
        gFull.fillRect(0, 0, fullScene.getWidth(), fullScene.getHeight());

        // Popped word rack for "DABA"
        gFull.setColor(new Color(25, 34, 58));
        gFull.fillRoundRect(15, 15, gridW - 30, 75, 14, 14);
        gFull.setColor(new Color(245, 175, 75));
        gFull.setStroke(new BasicStroke(1.5f));
        gFull.drawRoundRect(15, 15, gridW - 30, 75, 14, 14);

        char[] word = {'D', 'A', 'B', 'A'};
        int rackStartX = 30;
        for (int i = 0; i < word.length; i++) {
            BufferedImage tImg = ImageIO.read(new File("src/juanword/letters/" + word[i] + ".png"));
            drawTile(gFull, tImg, word[i], testPoints[i], rackStartX + i * 58, 24, 50, 56, true, false, false, false);
        }

        // Draw letter tray below
        gFull.drawImage(trayImg, 0, 100, null);
        gFull.dispose();

        File out = new File("scratch/test_letter_tile_grid.png");
        ImageIO.write(fullScene, "png", out);
        ImageIO.write(fullScene, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/test_letter_tile_grid.png"));
        System.out.println("Saved preview to " + out.getAbsolutePath());
    }

    private static void drawTile(Graphics2D g2, BufferedImage tileImg, char letter, int points,
                                 int x, int y, int w, int h, boolean selected, boolean hovered,
                                 boolean isGold, boolean isHeal) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int drawX = x;
        int drawY = y;
        int drawW = w;
        int drawH = h;

        if (selected) {
            // Golden glow around selected tile
            g.setColor(new Color(245, 180, 50, 160));
            g.setStroke(new BasicStroke(4.0f));
            g.drawRoundRect(drawX - 2, drawY - 2, drawW + 4, drawH + 4, 12, 12);
        } else if (hovered) {
            // Sky blue hover aura
            g.setColor(new Color(90, 190, 255, 160));
            g.setStroke(new BasicStroke(3.0f));
            g.drawRoundRect(drawX - 1, drawY - 1, drawW + 2, drawH + 2, 12, 12);
        }

        // Draw high-resolution tile asset
        g.drawImage(tileImg, drawX, drawY, drawW, drawH, null);

        if (selected) {
            // Subtle warm highlight
            Graphics2D gTint = (Graphics2D) g.create();
            gTint.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.15f));
            gTint.setColor(new Color(255, 200, 50));
            gTint.fillRoundRect(drawX, drawY, drawW, drawH, 10, 10);
            gTint.dispose();

            // Gold border
            g.setColor(new Color(240, 150, 30));
            g.setStroke(new BasicStroke(2.2f));
            g.drawRoundRect(drawX, drawY, drawW, drawH, 10, 10);
        } else if (hovered) {
            g.setColor(new Color(110, 200, 255));
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(drawX, drawY, drawW, drawH, 10, 10);
        }

        // Subscript Point Value Badge in Bottom-Right
        String ptStr = String.valueOf(points);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int pw = fm.stringWidth(ptStr);
        int px = drawX + drawW - pw - 5;
        int py = drawY + drawH - 5;

        // Clean subtle pill badge
        g.setColor(new Color(255, 255, 255, 190));
        g.fillRoundRect(px - 3, py - fm.getAscent() + 1, pw + 6, fm.getAscent() + 2, 4, 4);
        g.setColor(new Color(210, 195, 175));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(px - 3, py - fm.getAscent() + 1, pw + 6, fm.getAscent() + 2, 4, 4);

        g.setColor(selected ? new Color(200, 80, 20) : new Color(30, 40, 60));
        g.drawString(ptStr, px, py);

        // Decorator Badges (Gold / Healing)
        if (isGold) {
            g.setColor(new Color(255, 215, 0));
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            g.drawString("⭐", drawX + 3, drawY + 13);
        } else if (isHeal) {
            g.setColor(new Color(255, 75, 90));
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            g.drawString("💚", drawX + 3, drawY + 13);
        }

        g.dispose();
    }
}
