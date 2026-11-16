package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckGudoAttack {
    public static void main(String[] args) throws Exception {
        BufferedImage atk = ImageIO.read(new File("assets/gudoattack.png"));
        int leftPixels = 0, rightPixels = 0;
        int mid = atk.getWidth() / 2;
        int minX = atk.getWidth(), maxX = 0;
        for (int y = 0; y < atk.getHeight(); y++) {
            for (int x = 0; x < atk.getWidth(); x++) {
                int a = (atk.getRGB(x, y) >> 24) & 0xff;
                if (a > 30) {
                    if (x < mid) leftPixels++;
                    else rightPixels++;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }
        }
        System.out.printf("Attack: minX=%d, maxX=%d, Left=%d, Right=%d\n", minX, maxX, leftPixels, rightPixels);
    }
}
