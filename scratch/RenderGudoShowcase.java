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
import juanword.model.DifficultySetting;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.MapSelectPanel;

public class RenderGudoShowcase {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering Gudo Showcase Screenshots...");
        File artifactDir = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5");
        if (!artifactDir.exists()) artifactDir.mkdirs();

        JuanWord app = new JuanWord();

        // 1. Map Select Screen with 'Fight Gudo' button (unlocked via Cardo or beating Boss Porter)
        Player cardo = new Player(3, "Cardo");
        cardo.setUnlockedMap(4);
        cardo.recordDefeat("BOSS_PORTER");
        app.setActivePlayer(cardo);

        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();

        BufferedImage mapImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mapImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        mapPanel.paint(g2);
        g2.dispose();
        ImageIO.write(mapImg, "png", new File(artifactDir, "map_select_fight_gudo_unlocked.png"));
        System.out.println("  ✓ Rendered map_select_fight_gudo_unlocked.png");

        // 2. Gudo Boss Battle - Idle Stance
        Player juan = new Player(1, "Juan");
        juan.setUnlockedMap(4);
        BattleSession gudoSession = new BattleSessionBuilder()
            .withPlayer(juan)
            .onMap(4)
            .atStage(4)
            .build();

        BattlePanel gudoBattle = new BattlePanel(app);
        gudoBattle.setSize(960, 540);
        gudoBattle.begin(gudoSession);
        // Let entrance complete
        gudoBattle.getEnemyAnimator().completeEntrance();
        gudoBattle.getEnemyAnimator().update(300);
        gudoBattle.getPlayerAnimator().update(300);

        BufferedImage gudoIdle = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Idle = gudoIdle.createGraphics();
        g2Idle.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gudoBattle.paint(g2Idle);
        g2Idle.dispose();
        ImageIO.write(gudoIdle, "png", new File(artifactDir, "gudo_battle_idle.png"));
        System.out.println("  ✓ Rendered gudo_battle_idle.png");

        // 3. Gudo Boss Battle - Attack Strike Frame (gudoattack.png lunging left)
        gudoBattle.getEnemyAnimator().startAttack(AttackType.PUNCH);
        gudoBattle.getEnemyAnimator().update(150); // Mid-strike frame
        gudoBattle.getPlayerAnimator().triggerHit();

        BufferedImage gudoStrike = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Strike = gudoStrike.createGraphics();
        g2Strike.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gudoBattle.paint(g2Strike);
        g2Strike.dispose();
        ImageIO.write(gudoStrike, "png", new File(artifactDir, "gudo_battle_strike.png"));
        System.out.println("  ✓ Rendered gudo_battle_strike.png");

        // 4. Battle with Gudo as Playable Character
        Player gudoPlayer = new Player(2, "Gudo");
        gudoPlayer.setUnlockedMap(4);
        BattleSession playerGudoSession = new BattleSessionBuilder()
            .withPlayer(gudoPlayer)
            .onMap(1)
            .atStage(1)
            .build();

        BattlePanel playerGudoBattle = new BattlePanel(app);
        playerGudoBattle.setSize(960, 540);
        playerGudoBattle.begin(playerGudoSession);
        playerGudoBattle.getPlayerAnimator().update(250);

        BufferedImage gudoPlayerImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2P = gudoPlayerImg.createGraphics();
        g2P.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        playerGudoBattle.paint(g2P);
        g2P.dispose();
        ImageIO.write(gudoPlayerImg, "png", new File(artifactDir, "gudo_player_idle.png"));
        System.out.println("  ✓ Rendered gudo_player_idle.png");

        System.out.println("All Gudo Showcase screenshots rendered successfully!");
        System.exit(0);
    }
}
