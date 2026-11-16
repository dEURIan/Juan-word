package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class InspectDurian {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("assets/maps/durianmuseum.png"));
        int w = img.getWidth(), h = img.getHeight();

        // Print characteristics every 50 rows from 1000 to 1900
        for (int y = 1000; y < h; y += 50) {
            int uniqueColors = 0;
            int lastRgb = 0;
            int changes = 0;
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                if (x > 0 && rgb != lastRgb) changes++;
                lastRgb = rgb;
            }
            System.out.printf("Row %d: horizontal color changes = %d\n", y, changes);
        }
    }
}
