import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectBounds {
    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"indigframe.png", "mtapo.png", "mtapowithbg.png"}) {
            File f = new File(name);
            if (!f.exists()) continue;
            BufferedImage img = ImageIO.read(f);
            int minX = img.getWidth(), maxX = 0, minY = img.getHeight(), maxY = 0;
            int nonTrans = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int a = (img.getRGB(x, y) >> 24) & 0xff;
                    if (a > 10) {
                        nonTrans++;
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.printf("%s: non-trans pixels=%d, bounds=[%d,%d to %d,%d] (size %dx%d)%n",
                name, nonTrans, minX, minY, maxX, maxY, (maxX - minX + 1), (maxY - minY + 1));
        }
    }
}
