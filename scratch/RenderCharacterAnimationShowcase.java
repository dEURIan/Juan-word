package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.AttackType;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;

public class RenderCharacterAnimationShowcase {
    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        JuanWord app = new JuanWord();
        Player player = new Player(1, "Juan");
        app.setActivePlayer(player);

        String artifactDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";
        new File(artifactDir).mkdirs();

        // Frame 1: Idle Breathing Stance
        BattleSession s1 = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
        BattlePanel bp1 = new BattlePanel(app);
        bp1.setSize(960, 540);
        bp1.begin(s1);
        bp1.getPlayerAnimator().update(300); // 300ms idle breathing

        BufferedImage frame1 = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = frame1.createGraphics();
        bp1.paint(g1);
        g1.dispose();
        ImageIO.write(frame1, "png", new File(artifactDir, "character_anim_1_idle.png"));

        // Frame 2: Pre-Attack Stance (Wind-up / Anticipation)
        BattleSession s2 = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
        BattlePanel bp2 = new BattlePanel(app);
        bp2.setSize(960, 540);
        bp2.begin(s2);
        bp2.getPlayerAnimator().startAttack(AttackType.SLASH);
        bp2.getPlayerAnimator().update(60); // 60ms into attack (preattack phase)

        BufferedImage frame2 = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = frame2.createGraphics();
        bp2.paint(g2);
        g2.dispose();
        ImageIO.write(frame2, "png", new File(artifactDir, "character_anim_2_preattack.png"));

        // Frame 3: Attack Strike Frame (Lunge & Slash Impact)
        BattleSession s3 = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
        BattlePanel bp3 = new BattlePanel(app);
        bp3.setSize(960, 540);
        bp3.begin(s3);
        bp3.getPlayerAnimator().startAttack(AttackType.SLASH);
        bp3.getPlayerAnimator().update(180); // 180ms (impact point with forward lunge)

        BufferedImage frame3 = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g3 = frame3.createGraphics();
        bp3.paint(g3);
        g3.dispose();
        ImageIO.write(frame3, "png", new File(artifactDir, "character_anim_3_attack.png"));

        // Frame 4: Recovery Frame (Return to Idle)
        BattleSession s4 = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
        BattlePanel bp4 = new BattlePanel(app);
        bp4.setSize(960, 540);
        bp4.begin(s4);
        bp4.getPlayerAnimator().startAttack(AttackType.SLASH);
        bp4.getPlayerAnimator().update(300); // 300ms (recovery phase)

        BufferedImage frame4 = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g4 = frame4.createGraphics();
        bp4.paint(g4);
        g4.dispose();
        ImageIO.write(frame4, "png", new File(artifactDir, "character_anim_4_recovery.png"));

        // Create a 2x2 Composite Showcase (1920x1080)
        BufferedImage composite = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = composite.createGraphics();
        cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        cg.drawImage(frame1, 0, 0, 960, 540, null);
        cg.drawImage(frame2, 960, 0, 960, 540, null);
        cg.drawImage(frame3, 0, 540, 960, 540, null);
        cg.drawImage(frame4, 960, 540, 960, 540, null);

        // Overlay Labels
        drawLabel(cg, 30, 40, "1. IDLE STANCE (maincharacteridle.png - Breathing & Shadow Sync)");
        drawLabel(cg, 990, 40, "2. PRE-ATTACK WIND-UP (maincharapreattack.png - Anticipation Pullback)");
        drawLabel(cg, 30, 580, "3. ATTACK STRIKE (maincharattack.png - Forward Lunge + Slash Impact)");
        drawLabel(cg, 990, 580, "4. RECOVERY (Smooth Return to Idle Stance)");

        cg.dispose();
        ImageIO.write(composite, "png", new File(artifactDir, "character_animation_showcase.png"));

        System.out.println("✓ Character animation frames and composite showcase rendered successfully to artifact dir!");
        System.exit(0);
    }

    private static void drawLabel(Graphics2D g, int x, int y, String text) {
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        g.setColor(new Color(10, 20, 40, 220));
        g.fillRoundRect(x - 10, y - 24, tw + 20, 34, 10, 10);
        g.setColor(new Color(255, 195, 25)); // Gold
        g.drawRoundRect(x - 10, y - 24, tw + 20, 34, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }
}
