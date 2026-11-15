import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class TestButtonSizeVisuals {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                // Test 1: 192x48 with title 0.56
                renderVariation(192, 48, 0.56, "scratch/preview_192x48.png");
                // Test 2: 200x50 with title 0.55
                renderVariation(200, 50, 0.55, "scratch/preview_200x50.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }

    private static void renderVariation(int btnW, int btnH, double titleScale, String outPath) throws Exception {
        JuanWord app = new JuanWord();
        Field mcField = JuanWord.class.getDeclaredField("mainContainer");
        mcField.setAccessible(true);
        JPanel mc = (JPanel) mcField.get(app);

        mc.setSize(960, 540);
        mc.doLayout();

        Component startScreen = mc.getComponent(0);
        startScreen.setSize(960, 540);
        startScreen.doLayout();

        BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        startScreen.paint(g2);
        g2.dispose();

        ImageIO.write(img, "png", new File(outPath));
    }
}
