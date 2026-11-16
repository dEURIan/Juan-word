package scratch;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.AttackType;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;

public class RenderSpriteScalingShowcase {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering Sprite Scaling Showcase (Windowed 960x540 vs Fullscreen 1920x1080)...");
        File artifactDir = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5");

        JuanWord app = new JuanWord();
        Player player = new Player(1, "Juan");
        player.setUnlockedMap(4);

        // 1. Battle at Windowed 960x540 (Bigger in general: 155px vs old 130px)
        BattleSession session960 = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(4)
            .atStage(4) // Gudo Boss
            .build();

        BattlePanel battle960 = new BattlePanel(app);
        battle960.setSize(960, 540);
        battle960.begin(session960);
        battle960.getEnemyAnimator().completeEntrance();
        battle960.getPlayerAnimator().update(200);
        battle960.getEnemyAnimator().update(200);

        BufferedImage img960 = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2_960 = img960.createGraphics();
        g2_960.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        battle960.paint(g2_960);
        g2_960.dispose();
        ImageIO.write(img960, "png", new File(artifactDir, "battle_sprites_windowed_960.png"));
        System.out.println("  ✓ Rendered battle_sprites_windowed_960.png");

        // 2. Battle at Fullscreen 1920x1080 (Scaled up with responsive fullscreen boost!)
        app.setFullScreen(true);
        BattleSession session1080 = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(4)
            .atStage(4) // Gudo Boss
            .build();

        BattlePanel battle1080 = new BattlePanel(app);
        battle1080.setSize(1920, 1080);
        battle1080.begin(session1080);
        battle1080.getEnemyAnimator().completeEntrance();
        battle1080.getPlayerAnimator().update(200);
        battle1080.getEnemyAnimator().update(200);

        BufferedImage img1080 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2_1080 = img1080.createGraphics();
        g2_1080.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        battle1080.paint(g2_1080);
        g2_1080.dispose();
        ImageIO.write(img1080, "png", new File(artifactDir, "battle_sprites_fullscreen_1080.png"));
        System.out.println("  ✓ Rendered battle_sprites_fullscreen_1080.png");

        // 3. Side-by-side comparison collage
        int compW = 1920;
        int compH = 1080;
        BufferedImage compImg = new BufferedImage(compW, compH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2C = compImg.createGraphics();
        g2C.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2C.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Dark background
        g2C.setColor(new java.awt.Color(12, 14, 32));
        g2C.fillRect(0, 0, compW, compH);

        // Draw 960x540 scaled preview on top/left
        int halfW = (compW - 40) / 2;
        int halfH = (int) Math.round(halfW * (540.0 / 960.0));
        int py = (compH - halfH) / 2;
        g2C.drawImage(img960, 15, py, halfW, halfH, null);
        g2C.setColor(new java.awt.Color(255, 215, 60));
        g2C.setStroke(new java.awt.BasicStroke(2.0f));
        g2C.drawRect(15, py, halfW, halfH);
        g2C.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 18));
        g2C.drawString("WINDOWED (960x540) - Base Sprite Height: 155px (+19%)", 25, py - 12);

        // Draw 1080p scaled preview on right
        g2C.drawImage(img1080, 25 + halfW, py, halfW, halfH, null);
        g2C.drawRect(25 + halfW, py, halfW, halfH);
        g2C.drawString("FULLSCREEN (1920x1080) - Boosted Height: 366px (+41%)", 35 + halfW, py - 12);

        g2C.dispose();
        ImageIO.write(compImg, "png", new File(artifactDir, "sprites_scaling_comparison.png"));
        System.out.println("  ✓ Rendered sprites_scaling_comparison.png");

        System.out.println("Done!");
        System.exit(0);
    }
}
