import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectLayers {
    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"bg.png", "mtapo.png", "indigframe.png", "TITLE_PAGE.png"}) {
            File f = new File(name);
            if (!f.exists()) {
                System.out.println(name + " does not exist");
                continue;
            }
            BufferedImage img = ImageIO.read(f);
            int minA = 255, maxA = 0;
            for (int y = 0; y < img.getHeight(); y += 50) {
                for (int x = 0; x < img.getWidth(); x += 50) {
                    int a = (img.getRGB(x, y) >> 24) & 0xff;
                    if (a < minA) minA = a;
                    if (a > maxA) maxA = a;
                }
            }
            System.out.println(name + ": " + img.getWidth() + "x" + img.getHeight() + ", alpha min=" + minA + ", max=" + maxA);
        }
    }
}
