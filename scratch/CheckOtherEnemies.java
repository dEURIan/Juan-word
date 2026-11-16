package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckOtherEnemies {
    public static void main(String[] args) throws Exception {
        for (String f : new String[]{"porter.png", "alviola.png", "dizon.png", "janitor.png", "securityguard.png"}) {
            BufferedImage img = ImageIO.read(new File("assets/sprites/" + f));
            int l = 0, r = 0, mid = img.getWidth() / 2;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int a = (img.getRGB(x, y) >> 24) & 0xff;
                    if (a > 30) {
                        if (x < mid) l++; else r++;
                    }
                }
            }
            System.out.printf("%s: Left=%d, Right=%d\n", f, l, r);
        }
    }
}
