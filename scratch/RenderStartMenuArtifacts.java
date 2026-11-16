package scratch;

import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.StartPanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

public class RenderStartMenuArtifacts {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering StartMenu artifacts across resolutions...");
        JuanWord host = new JuanWord();
        StartPanel sp = new StartPanel(host);

        Player p = new Player(1, "Juan");
        p.setScore(310);
        p.setUnlockedMap(1);
        host.setActivePlayer(p);
        sp.refreshProfiles();

        String artDir = "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5";

        // 1. Fullscreen (1920x1080)
        BufferedImage imgFs = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D gFs = imgFs.createGraphics();
        sp.setSize(1920, 1080);
        sp.paint(gFs);
        gFs.dispose();
        ImageIO.write(imgFs, "png", new File(artDir, "menu_fullscreen_1080.png"));

        // Verify Title and Button bounds at 1920x1080
        Rectangle titleFs = sp.getTitleBounds();
        Rectangle contFs = sp.getContinueBtnBounds();
        Rectangle exitFs = sp.getExitBtnBounds();
        Rectangle cardFs = sp.getPlayerCardBounds();

        System.out.println("Fullscreen 1080p: Title: " + titleFs);
        System.out.println("Fullscreen 1080p: ContinueBtn: " + contFs);
        System.out.println("Fullscreen 1080p: ExitBtn: " + exitFs);
        System.out.println("Fullscreen 1080p: PlayerCard: " + cardFs);

        assert titleFs.y + titleFs.height < contFs.y : "Title must end strictly above Continue button! TitleBottom=" + (titleFs.y + titleFs.height) + ", ContinueTop=" + contFs.y;
        assert contFs.x == (1920 - contFs.width) / 2 : "Continue button must be horizontally centered! Expected=" + ((1920 - contFs.width) / 2) + ", Actual=" + contFs.x;
        assert exitFs.y + exitFs.height <= 1080 : "Exit button must stay inside screen! ExitBottom=" + (exitFs.y + exitFs.height);

        // 2. Windowed (960x540)
        BufferedImage imgWin = new BufferedImage(960, 540, BufferedImage.TYPE_INT_RGB);
        Graphics2D gWin = imgWin.createGraphics();
        sp.setSize(960, 540);
        sp.paint(gWin);
        gWin.dispose();
        ImageIO.write(imgWin, "png", new File(artDir, "menu_windowed_960.png"));

        Rectangle titleWin = sp.getTitleBounds();
        Rectangle contWin = sp.getContinueBtnBounds();
        assert titleWin.y + titleWin.height < contWin.y : "Windowed: Title must end strictly above Continue button!";

        System.out.println("Artifacts successfully generated and verified!");
        System.exit(0);
    }
}
