package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class InspectLandmarkSprites {
    public static void main(String[] args) throws Exception {
        BufferedImage bb = ImageIO.read(new File("assets/maps/Blue Building.png"));
        BufferedImage sp = ImageIO.read(new File("assets/maps/sanpedro.png"));
        BufferedImage dm = ImageIO.read(new File("assets/maps/durianmuseum.png"));
        BufferedImage ma = ImageIO.read(new File("assets/maps/mtapo.png"));

        System.out.printf("Blue Building.png: %dx%d\n", bb.getWidth(), bb.getHeight());
        System.out.printf("sanpedro.png: %dx%d\n", sp.getWidth(), sp.getHeight());
        System.out.printf("durianmuseum.png: %dx%d\n", dm.getWidth(), dm.getHeight());
        System.out.printf("mtapo.png: %dx%d\n", ma.getWidth(), ma.getHeight());

        // Check dominant colors
        printColorStats("Blue Building", bb);
        printColorStats("San Pedro", sp);
        printColorStats("Durian Museum", dm);
    }

    private static void printColorStats(String name, BufferedImage img) {
        long r = 0, g = 0, b = 0, count = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                if (a > 30) {
                    count++;
                    r += (rgb >> 16) & 0xff;
                    g += (rgb >> 8) & 0xff;
                    b += rgb & 0xff;
                }
            }
        }
        if (count > 0) {
            System.out.printf("%s: %d non-transparent pixels, avg color: R=%d G=%d B=%d\n",
                name, count, r/count, g/count, b/count);
        }
    }
}
