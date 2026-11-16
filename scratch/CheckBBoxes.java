package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckBBoxes {
    public static void main(String[] args) throws Exception {
        for (String file : new String[]{"Blue Building.png", "durianmuseum.png", "sanpedro.png", "mtapo.png"}) {
            BufferedImage img = ImageIO.read(new File("assets/maps/" + file));
            int minX = img.getWidth(), maxX = 0, minY = img.getHeight(), maxY = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int a = (img.getRGB(x, y) >> 24) & 0xff;
                    if (a > 20) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.printf("%s: total=%dx%d, bbox=[x=%d, y=%d, w=%d, h=%d]\n",
                file, img.getWidth(), img.getHeight(), minX, minY, (maxX - minX + 1), (maxY - minY + 1));
        }
    }
}
