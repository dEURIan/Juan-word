package scratch;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.tile.TileComponent;
import juanword.ui.BattlePanel;
import juanword.ui.MapSelectPanel;
import juanword.ui.StartPanel;

public class RenderDesignCheck {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        Player player = new Player(1, "Juan");
        app.setActivePlayer(player);

        // 1. Render StartPanel
        StartPanel startPanel = new StartPanel(app);
        startPanel.setSize(960, 540);
        startPanel.doLayout();
        BufferedImage startImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gStart = startImg.createGraphics();
        startPanel.paint(gStart);
        gStart.dispose();
        ImageIO.write(startImg, "PNG", new File("scratch/restored_start_panel.png"));
        System.out.println("Rendered scratch/restored_start_panel.png");

        // 2. Render MapSelectPanel (Default Map 1 unlocked)
        MapSelectPanel mapPanel = new MapSelectPanel(app);
        mapPanel.setSize(960, 540);
        mapPanel.refresh();
        mapPanel.doLayout();
        BufferedImage mapImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMap = mapImg.createGraphics();
        mapPanel.paint(gMap);
        gMap.dispose();
        ImageIO.write(mapImg, "PNG", new File("scratch/restored_map_panel.png"));
        System.out.println("Rendered scratch/restored_map_panel.png");

        // 2b. Render MapSelectPanel with Maps 1-3 unlocked
        player.unlockMap(3);
        mapPanel.refresh();
        mapPanel.doLayout();
        BufferedImage mapUnlockedImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMapUn = mapUnlockedImg.createGraphics();
        mapPanel.paint(gMapUn);
        gMapUn.dispose();
        ImageIO.write(mapUnlockedImg, "PNG", new File("scratch/restored_map_unlocked.png"));
        System.out.println("Rendered scratch/restored_map_unlocked.png");

        // 2c. Render MapSelectPanel with all 4 maps unlocked (Mount Apo unlocked!)
        player.unlockMap(4);
        mapPanel.refresh();
        mapPanel.doLayout();
        BufferedImage map4UnlockedImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMap4 = map4UnlockedImg.createGraphics();
        mapPanel.paint(gMap4);
        gMap4.dispose();
        ImageIO.write(map4UnlockedImg, "PNG", new File("scratch/restored_map_apo_unlocked.png"));
        System.out.println("Rendered scratch/restored_map_apo_unlocked.png");

        // 2d. Render MapSelectPanel with Mount Apo hovered (locked state, Map 1 unlocked)
        player.setUnlockedMap(1);
        mapPanel.refresh();
        mapPanel.doLayout();
        // Simulate mouse move over Mt Apo summit at (480, 160)
        java.awt.event.MouseEvent meMove = new java.awt.event.MouseEvent(
            mapPanel, java.awt.event.MouseEvent.MOUSE_MOVED,
            System.currentTimeMillis(), 0, 480, 160, 0, false
        );
        for (java.awt.event.MouseMotionListener mml : mapPanel.getMouseMotionListeners()) {
            mml.mouseMoved(meMove);
        }
        BufferedImage mapApoHoverLocked = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gApoHoverLocked = mapApoHoverLocked.createGraphics();
        mapPanel.paint(gApoHoverLocked);
        gApoHoverLocked.dispose();
        ImageIO.write(mapApoHoverLocked, "PNG", new File("scratch/restored_map_apo_hover_locked.png"));
        System.out.println("Rendered scratch/restored_map_apo_hover_locked.png");

        // 2e. Render MapSelectPanel with Mount Apo hovered (unlocked state, Map 4 unlocked)
        player.unlockMap(4);
        mapPanel.refresh();
        mapPanel.doLayout();
        for (java.awt.event.MouseMotionListener mml : mapPanel.getMouseMotionListeners()) {
            mml.mouseMoved(meMove);
        }
        BufferedImage mapApoHoverUnlocked = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gApoHoverUnlocked = mapApoHoverUnlocked.createGraphics();
        mapPanel.paint(gApoHoverUnlocked);
        gApoHoverUnlocked.dispose();
        ImageIO.write(mapApoHoverUnlocked, "PNG", new File("scratch/restored_map_apo_hover_unlocked.png"));
        System.out.println("Rendered scratch/restored_map_apo_hover_unlocked.png");

        player.setUnlockedMap(1); // reset

        // 3. Render BattlePanel
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(player)
            .onMap(1)
            .atStage(1)
            .build();
        // Ensure grid contains Gold and Healing tiles for visual inspection
        TileComponent[][] grid = session.getGrid();
        grid[0][1] = new juanword.tile.GoldTileDecorator(new juanword.tile.StandardTileComponent('G'));
        grid[1][2] = new juanword.tile.HealingTileDecorator(new juanword.tile.StandardTileComponent('H'));
        grid[2][3] = new juanword.tile.GoldTileDecorator(new juanword.tile.StandardTileComponent('O'));
        grid[3][0] = new juanword.tile.HealingTileDecorator(new juanword.tile.StandardTileComponent('E'));

        BattlePanel battlePanel = new BattlePanel(app);
        battlePanel.setSize(960, 540);
        battlePanel.begin(session);
        battlePanel.getEnemyAnimator().completeEntrance();
        battlePanel.doLayout();
        BufferedImage battleImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gBattle = battleImg.createGraphics();
        battlePanel.paint(gBattle);
        gBattle.dispose();
        ImageIO.write(battleImg, "PNG", new File("scratch/restored_battle_panel.png"));
        System.out.println("Rendered scratch/restored_battle_panel.png");

        System.exit(0);
    }
}
