package scratch;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.MapSelectPanel;
import juanword.ui.StartPanel;

public class RenderLatestScreenshots {
    public static void main(String[] args) throws Exception {
        DataManager dm = DataManager.getInstance();
        JuanWord app = new JuanWord();
        Player player = new Player(1, "Cardo");
        player.setUnlockedMap(4);
        player.setScore(9999);
        app.setActivePlayer(player);

        // 1. Render MapSelectPanel (swapped San Pedro left & Blue Building right, non-stretched)
        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();
        mapPanel.doLayout();
        BufferedImage mapImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMap = mapImg.createGraphics();
        mapPanel.paint(gMap);
        gMap.dispose();
        String artifactDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";
        ImageIO.write(mapImg, "PNG", new File(artifactDir, "latest_map_select_bigger_icons.png"));
        ImageIO.write(mapImg, "PNG", new File("scratch/latest_map_select_swapped.png"));
        System.out.println("Rendered scratch/latest_map_select_swapped.png and artifact latest_map_select_bigger_icons.png");

        // 2. Render BattlePanel with Map 1 background (bg.png + blue building covering most of screen) and ATAKE unlit
        BattleSession s1 = new BattleSessionBuilder().withPlayer(player).onMap(1).atStage(1).build();
        BattlePanel b1 = new BattlePanel(app);
        b1.setSize(960, 540);
        b1.begin(s1);
        b1.doLayout();
        BufferedImage b1Img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gb1 = b1Img.createGraphics();
        b1.paint(gb1);
        gb1.dispose();
        ImageIO.write(b1Img, "PNG", new File(artifactDir, "latest_battle_m1_unlit.png"));
        ImageIO.write(b1Img, "PNG", new File("scratch/latest_battle_m1_unlit.png"));
        System.out.println("Rendered scratch/latest_battle_m1_unlit.png");

        // 3. Render BattlePanel with Map 2 background (bg.png + cropped durian museum covering most of screen)
        BattleSession s2 = new BattleSessionBuilder().withPlayer(player).onMap(2).atStage(1).build();
        BattlePanel b2 = new BattlePanel(app);
        b2.setSize(960, 540);
        b2.begin(s2);
        b2.doLayout();
        BufferedImage b2Img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gb2 = b2Img.createGraphics();
        b2.paint(gb2);
        gb2.dispose();
        ImageIO.write(b2Img, "PNG", new File(artifactDir, "latest_battle_m2_durian.png"));
        ImageIO.write(b2Img, "PNG", new File("scratch/latest_battle_m2_durian.png"));
        System.out.println("Rendered scratch/latest_battle_m2_durian.png");

        // 4. Render BattlePanel with Map 3 background (bg.png + san pedro cathedral covering most of screen)
        BattleSession s3 = new BattleSessionBuilder().withPlayer(player).onMap(3).atStage(1).build();
        BattlePanel b3 = new BattlePanel(app);
        b3.setSize(960, 540);
        b3.begin(s3);
        b3.doLayout();
        BufferedImage b3Img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gb3 = b3Img.createGraphics();
        b3.paint(gb3);
        gb3.dispose();
        ImageIO.write(b3Img, "PNG", new File(artifactDir, "latest_battle_m3_sanpedro.png"));
        ImageIO.write(b3Img, "PNG", new File("scratch/latest_battle_m3_sanpedro.png"));
        System.out.println("Rendered scratch/latest_battle_m3_sanpedro.png");
        System.exit(0);
    }
}
