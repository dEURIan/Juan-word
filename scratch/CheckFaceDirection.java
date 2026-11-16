package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckFaceDirection {
    public static void main(String[] args) throws Exception {
        BufferedImage gudo = ImageIO.read(new File("assets/gudoidle.png"));
        int leftPixels = 0;
        int rightPixels = 0;
        int mid = gudo.getWidth() / 2;
        for (int y = 0; y < gudo.getHeight(); y++) {
            for (int x = 0; x < gudo.getWidth(); x++) {
                int a = (gudo.getRGB(x, y) >> 24) & 0xff;
                if (a > 30) {
                    if (x < mid) leftPixels++;
                    else rightPixels++;
                }
            }
        }
        System.out.printf("Left pixels: %d, Right pixels: %d\n", leftPixels, rightPixels);
    }
}
