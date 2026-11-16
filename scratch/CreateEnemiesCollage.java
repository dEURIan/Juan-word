package scratch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.model.DifficultySetting;
import juanword.ui.ImageLoader;

public class CreateEnemiesCollage {
    public static void main(String[] args) throws Exception {
        int cellW = 280;
        int cellH = 220;
        int cols = 3;
        int rows = 4;
        int totalW = cellW * cols + 40;
        int totalH = cellH * rows + 80;

        BufferedImage collage = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = collage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Dark navy background
        g2.setColor(new Color(15, 20, 35));
        g2.fillRect(0, 0, totalW, totalH);

        // Header
        g2.setColor(new Color(255, 215, 80));
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 22));
        g2.drawString("JUANWORD - ALL 12 MAP ENEMY SPRITES", 24, 38);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.setColor(new Color(180, 200, 230));
        g2.drawString("Official Character Assets from Documentation • All Placeholders Removed", 24, 58);

        String[] mapTitles = {
            "MAP 1: ATENEO BLUE BUILDING (SAYON)",
            "MAP 2: DAVAO MUSEUM (SAKTO)",
            "MAP 3: SAN PEDRO CATHEDRAL (LISOD)",
            "MAP 4: MOUNT APO (EXTREME)"
        };

        for (int map = 1; map <= rows; map++) {
            for (int stage = 1; stage <= cols; stage++) {
                int x = 20 + (stage - 1) * cellW;
                int y = 70 + (map - 1) * cellH;

                // Card background
                g2.setColor(new Color(24, 32, 54));
                g2.fillRoundRect(x + 4, y + 4, cellW - 8, cellH - 8, 12, 12);
                g2.setColor(new Color(45, 60, 95));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x + 4, y + 4, cellW - 8, cellH - 8, 12, 12);

                Enemy enemy = EnemyFactory.createEnemy(map, stage, DifficultySetting.SAYON);
                BufferedImage sprite = ImageLoader.load(enemy.getSpritePath());

                // Title badge
                g2.setColor(enemy.isBoss() ? new Color(255, 90, 90) : new Color(255, 215, 80));
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                String stageLabel = "Stage " + stage + (enemy.isBoss() ? " [BOSS]" : "");
                g2.drawString(stageLabel, x + 16, y + 26);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                g2.drawString(enemy.getName(), x + 16, y + 46);

                // Draw sprite scaled nicely
                if (sprite != null) {
                    int maxH = 135;
                    int maxW = cellW - 40;
                    double scale = Math.min((double) maxH / sprite.getHeight(), (double) maxW / sprite.getWidth());
                    int sw = (int) Math.round(sprite.getWidth() * scale);
                    int sh = (int) Math.round(sprite.getHeight() * scale);
                    int sx = x + (cellW - sw) / 2;
                    int sy = y + cellH - sh - 16;

                    // Ground shadow
                    g2.setColor(new Color(0, 0, 0, 70));
                    g2.fillOval(sx + (sw - (int) (sw * 0.8)) / 2, y + cellH - 18, (int) (sw * 0.8), 8);

                    g2.drawImage(sprite, sx, sy, sw, sh, null);
                }
            }
        }

        g2.dispose();
        ImageIO.write(collage, "PNG", new File("scratch/all_map_enemies_showcase.png"));
        System.out.println("Saved scratch/all_map_enemies_showcase.png");
    }
}
