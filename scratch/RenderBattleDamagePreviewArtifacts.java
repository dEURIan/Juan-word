package scratch;

import juanword.JuanWord;
import juanword.model.*;
import juanword.ui.*;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class RenderBattleDamagePreviewArtifacts {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering battle damage preview artifacts...");
        JuanWord host = new JuanWord();
        BattlePanel bp = new BattlePanel(host);

        Player p = new Player(1, "Ikaw");
        p.setScore(150);
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(p)
            .onMap(1)
            .atStage(1)
            .build();
        bp.begin(session);

        // Select tiles: e.g. 3 tiles that form a word / damage preview
        Field pathField = BattlePanel.class.getDeclaredField("selectedPath");
        pathField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<int[]> path = (List<int[]>) pathField.get(bp);
        path.add(new int[]{0, 0});
        path.add(new int[]{0, 1});
        path.add(new int[]{0, 2});

        String artDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";

        // 1. Windowed (960x540)
        BufferedImage imgWin = new BufferedImage(960, 540, BufferedImage.TYPE_INT_RGB);
        Graphics2D gWin = imgWin.createGraphics();
        bp.setSize(960, 540);
        bp.paint(gWin);
        gWin.dispose();
        ImageIO.write(imgWin, "png", new File(artDir, "battle_damage_preview_windowed_960.png"));

        // 2. Fullscreen (1920x1080)
        BufferedImage imgFs = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D gFs = imgFs.createGraphics();
        bp.setSize(1920, 1080);
        bp.paint(gFs);
        gFs.dispose();
        ImageIO.write(imgFs, "png", new File(artDir, "battle_damage_preview_fullscreen_1080.png"));

        // 3. Close-up of enemy HP bar (windowed crop and fullscreen crop)
        // Enemy card in windowed: enemyX = cardX + cardW - enemyW = 20 + (960 - 40) - 185 = 755
        // eHpBarX = 755 + (185 - 160)/2 = 767, y = 54 + 172 = 226, w = 160, h = 60
        BufferedImage cropWin = imgWin.getSubimage(740, 210, 190, 75);
        ImageIO.write(cropWin, "png", new File(artDir, "enemy_hp_bar_preview_crop.png"));

        System.out.println("Artifacts successfully generated!");
        System.exit(0);
    }
}
