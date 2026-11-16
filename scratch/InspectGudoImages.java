package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class InspectGudoImages {
    public static void main(String[] args) throws Exception {
        String[] sprites = {
            "maincharacteridle.png", "maincharapreattack.png", "maincharattack.png",
            "gudoidle.png", "gudoattack.png",
            "securityguard.png", "student.png", "dizon.png",
            "painting1.png", "painting2.png", "janitor.png",
            "grandma.png", "altarboy.png", "alviola.png",
            "monkey.png", "karabaw.png", "porter.png"
        };
        for (String s : sprites) {
            File f = new File("assets/sprites/" + s);
            if (!f.exists()) f = new File("assets/" + s);
            if (!f.exists()) continue;
            BufferedImage img = ImageIO.read(f);
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
            System.out.printf("%-24s size=%4dx%4d bbox=[x=%3d, y=%3d, w=%3d, h=%3d] topPct=%.2f botPct=%.2f\n",
                s, img.getWidth(), img.getHeight(), minX, minY, (maxX - minX + 1), (maxY - minY + 1),
                minY / (double) img.getHeight(), maxY / (double) img.getHeight());
        }
    }
}
