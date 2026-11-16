package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestCropDurian {
    public static void main(String[] args) throws Exception {
        BufferedImage orig = ImageIO.read(new File("assets/maps/durianmuseum.png"));
        int w = orig.getWidth();
        int h = orig.getHeight();

        // Check for edge of structure: where does the image stop having complex color and start having uniform floor?
        for (int y = 1200; y < h; y += 40) {
            int nonFloorPixels = 0;
            for (int x = 0; x < w; x++) {
                int rgb = orig.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Floor is grayish tan: r~200, g~205, b~185
                boolean isFloor = Math.abs(r - 200) < 25 && Math.abs(g - 205) < 25 && Math.abs(b - 185) < 25;
                if (!isFloor) nonFloorPixels++;
            }
            System.out.printf("Row %d: non-floor pixels = %d / %d (%.1f%%)\n",
                y, nonFloorPixels, w, (100.0 * nonFloorPixels / w));
        }
    }
}
