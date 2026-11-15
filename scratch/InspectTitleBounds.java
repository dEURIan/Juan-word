import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectTitleBounds {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("Title.png"));
        int minX = img.getWidth(), maxX = 0, minY = img.getHeight(), maxY = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int a = (img.getRGB(x, y) >> 24) & 0xff;
                if (a > 10) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        System.out.printf("Title.png bounds: [%d,%d to %d,%d] in %dx%d%n",
            minX, minY, maxX, maxY, img.getWidth(), img.getHeight());
    }
}
