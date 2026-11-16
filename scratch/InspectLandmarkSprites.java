package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class InspectLandmarkSprites {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "assets/maps/bluebuilding.png",
            "assets/maps/durianmuseum.png",
            "assets/maps/sanpedro.png"
        };
        
        for (String path : files) {
            BufferedImage img = ImageIO.read(new File(path));
            int minX = img.getWidth(), maxX = 0;
            int minY = img.getHeight(), maxY = 0;
            
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                    if (alpha > 20) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.printf("%s: size=[%dx%d], opaque bbox=[x=%d..%d (w=%d), y=%d..%d (h=%d)]\n",
                path, img.getWidth(), img.getHeight(), minX, maxX, (maxX - minX + 1), minY, maxY, (maxY - minY + 1));
        }
        System.exit(0);
    }
}
