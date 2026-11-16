import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.model.AttackType;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.EnemyAnimator;
import juanword.ui.ImageLoader;

public class CaptureGudoNerfAndAnimation {
    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");

        String artifactDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";

        // 1. Verify Gudo stats
        Enemy gudo = EnemyFactory.createEnemy(4, 4, DifficultySetting.EXTREME);
        System.out.println("Gudo Nerfed Stats: Max HP = " + gudo.getMaxHp() + " (was 600), Attack = " + gudo.getAttack() + " (was 45)");

        // 2. Render Full Battle against Gudo at 1920x1080 and 960x540
        JuanWord app = new JuanWord();
        Player player = new Player(1, "Ikaw");
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(4)
            .atStage(4)
            .build();

        BattlePanel panel1080 = new BattlePanel(app);
        panel1080.setSize(1920, 1080);
        panel1080.begin(session);
        panel1080.doLayout();
        // Complete entrance
        panel1080.getEnemyAnimator().completeEntrance();

        BufferedImage img1080 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img1080.createGraphics();
        panel1080.paint(g2);
        g2.dispose();
        ImageIO.write(img1080, "png", new File(artifactDir, "gudo_battle_nerfed_1080.png"));

        // Render Battle with selected 4-letter word showing ghost damage boost
        java.lang.reflect.Field pathField = BattlePanel.class.getDeclaredField("selectedPath");
        pathField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<int[]> path = (java.util.List<int[]>) pathField.get(panel1080);
        path.add(new int[]{0, 0});
        path.add(new int[]{0, 1});
        path.add(new int[]{0, 2});
        path.add(new int[]{0, 3});

        BufferedImage imgGhostDmg = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gGhost = imgGhostDmg.createGraphics();
        panel1080.paint(gGhost);
        gGhost.dispose();
        ImageIO.write(imgGhostDmg, "png", new File(artifactDir, "gudo_battle_ghost_damage_1080.png"));

        // 3. Render 3-frame animation strip of Gudo's slow attack (800ms)
        BufferedImage idle = ImageLoader.load("gudoidle.png");
        BufferedImage attack = ImageLoader.load("gudoattack.png");
        EnemyAnimator ea = new EnemyAnimator();
        ea.startEntrance(400);
        ea.completeEntrance();
        ea.startAttack(AttackType.PUNCH, 800L);

        int frameW = 320;
        int frameH = 400;
        BufferedImage strip = new BufferedImage(frameW * 3, frameH + 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = strip.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        sg.setColor(new Color(24, 30, 48));
        sg.fillRect(0, 0, frameW * 3, frameH + 80);

        // Frame 1: Windup (150ms)
        ea.update(150);
        BufferedImage f1Sprite = ea.getActiveSprite(idle, attack);
        Graphics2D gF1 = (Graphics2D) sg.create(0, 0, frameW, frameH + 80);
        gF1.setColor(new Color(255, 255, 255));
        gF1.setFont(new Font("SansSerif", Font.BOLD, 18));
        gF1.drawString("1. Windup (150ms)", 20, 35);
        gF1.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gF1.setColor(new Color(180, 200, 230));
        gF1.drawString("Pull back & anticipation", 20, 58);
        ea.paint(gF1, f1Sprite, 80, 80, 160, 280, 361.0 / 500.0);
        gF1.dispose();

        // Frame 2: Strike Punch (450ms)
        ea.update(300); // 450ms total
        BufferedImage f2Sprite = ea.getActiveSprite(idle, attack);
        Graphics2D gF2 = (Graphics2D) sg.create(frameW, 0, frameW, frameH + 80);
        gF2.setColor(new Color(255, 215, 0));
        gF2.setFont(new Font("SansSerif", Font.BOLD, 18));
        gF2.drawString("2. Strike Punch (450ms)", 20, 35);
        gF2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gF2.setColor(new Color(255, 180, 80));
        gF2.drawString("gudoattack.png punch lunge", 20, 58);
        ea.paint(gF2, f2Sprite, 80, 80, 160, 280, 361.0 / 500.0);
        gF2.dispose();

        // Frame 3: Recovery (700ms)
        ea.update(250); // 700ms total
        BufferedImage f3Sprite = ea.getActiveSprite(idle, attack);
        Graphics2D gF3 = (Graphics2D) sg.create(frameW * 2, 0, frameW, frameH + 80);
        gF3.setColor(new Color(255, 255, 255));
        gF3.setFont(new Font("SansSerif", Font.BOLD, 18));
        gF3.drawString("3. Recovery (700ms)", 20, 35);
        gF3.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gF3.setColor(new Color(180, 200, 230));
        gF3.drawString("Smooth return to stance", 20, 58);
        ea.paint(gF3, f3Sprite, 80, 80, 160, 280, 361.0 / 500.0);
        gF3.dispose();

        // Frame dividers
        sg.setColor(new Color(60, 80, 120));
        sg.drawLine(frameW, 0, frameW, frameH + 80);
        sg.drawLine(frameW * 2, 0, frameW * 2, frameH + 80);

        sg.dispose();
        ImageIO.write(strip, "png", new File(artifactDir, "gudo_attack_animation_strip.png"));

        System.out.println("Captured visual artifacts: gudo_battle_nerfed_1080.png and gudo_attack_animation_strip.png");
    }
}
