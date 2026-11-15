package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckSprites {
    public static void main(String[] args) throws Exception {
        String[] names = {"maincharacteridle.png", "maincharapreattack.png", "maincharattack.png"};
        for (String name : names) {
            File f = new File("src/juanword/" + name);
            if (!f.exists()) {
                System.out.println("File not found: " + name);
                continue;
            }
            BufferedImage img = ImageIO.read(f);
            int minX = img.getWidth(), minY = img.getHeight(), maxX = -1, maxY = -1;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                    if (alpha > 10) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.printf("%s: %dx%d, non-alpha bounds=[%d, %d, %d, %d], contentSize=%dx%d\n",
                name, img.getWidth(), img.getHeight(), minX, minY, maxX, maxY, (maxX - minX + 1), (maxY - minY + 1));
        }
    }
}
