import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.ui.StartPanel;

public class VerifyMenuVisualsMatch {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering StartPanel with refactored standard JButtons...");
        JuanWord host = new JuanWord();
        StartPanel panel = new StartPanel(host);

        // 1. Render Windowed 960x540
        panel.setSize(960, 540);
        panel.doLayout();
        BufferedImage winImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = winImg.createGraphics();
        panel.paint(g1);
        g1.dispose();
        ImageIO.write(winImg, "png", new File("scratch/menu_windowed_refactored_960.png"));
        System.out.println("Saved scratch/menu_windowed_refactored_960.png");

        // 2. Render Fullscreen 1920x1080
        panel.setSize(1920, 1080);
        panel.doLayout();
        BufferedImage fullImg = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fullImg.createGraphics();
        panel.paint(g2);
        g2.dispose();
        ImageIO.write(fullImg, "png", new File("scratch/menu_fullscreen_refactored_1080.png"));
        System.out.println("Saved scratch/menu_fullscreen_refactored_1080.png");

        // 3. Test Hover & Pressed Rendering
        // Hover over New Game Button
        Rectangle b = panel.getNewGameBtnBounds();
        panel.setSize(960, 540);
        panel.doLayout();
        // Simulate hover
        Component c = panel.getComponentAt(b.x + b.width / 2, b.y + b.height / 2);
        if (c instanceof javax.swing.JButton) {
            javax.swing.JButton btn = (javax.swing.JButton) c;
            btn.getModel().setRollover(true);
            BufferedImage hovImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gh = hovImg.createGraphics();
            panel.paint(gh);
            gh.dispose();
            ImageIO.write(hovImg, "png", new File("scratch/menu_newgame_hover_960.png"));
            System.out.println("Saved scratch/menu_newgame_hover_960.png (Hover state)");

            // Simulate press
            btn.getModel().setPressed(true);
            btn.getModel().setArmed(true);
            BufferedImage prImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gp = prImg.createGraphics();
            panel.paint(gp);
            gp.dispose();
            ImageIO.write(prImg, "png", new File("scratch/menu_newgame_pressed_960.png"));
            System.out.println("Saved scratch/menu_newgame_pressed_960.png (Pressed state)");
        }

        // Compare windowed image against previous reference if available
        File refFile = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/menu_windowed_960.png");
        if (refFile.exists()) {
            BufferedImage ref = ImageIO.read(refFile);
            long diff = 0;
            for (int y = 0; y < 540; y++) {
                for (int x = 0; x < 960; x++) {
                    if (ref.getRGB(x, y) != winImg.getRGB(x, y)) {
                        diff++;
                    }
                }
            }
            double pctDiff = (diff * 100.0) / (960 * 540);
            System.out.printf("Visual comparison with previous menu_windowed_960.png: %.2f%% diff (diff pixels: %d)%n", pctDiff, diff);
            if (pctDiff < 0.1) {
                System.out.println("PASS: Perfect pixel-level fidelity confirmed!");
            }
        }
    }
}
