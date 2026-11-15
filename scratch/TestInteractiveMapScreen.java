import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestInteractiveMapScreen {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                app.setSize(960, 540);
                app.doLayout();
                System.out.println("Initialized JuanWord successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
