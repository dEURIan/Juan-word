import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

public class PreviewStartScreen {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                Field mcField = JuanWord.class.getDeclaredField("mainContainer");
                mcField.setAccessible(true);
                JPanel mc = (JPanel) mcField.get(app);

                mc.setSize(960, 540);
                mc.doLayout();
                
                Component startScreen = mc.getComponent(0);
                startScreen.setSize(960, 540);
                startScreen.doLayout();
                if (startScreen instanceof Container) {
                    for (Component c : ((Container) startScreen).getComponents()) {
                        c.doLayout();
                        if (c instanceof Container) {
                            for (Component c2 : ((Container) c).getComponents()) {
                                c2.doLayout();
                            }
                        }
                    }
                }

                BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                startScreen.paint(g2);
                g2.dispose();

                File out = new File("scratch/start_screen_preview.png");
                ImageIO.write(img, "png", out);
                System.out.println("Preview saved to " + out.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
