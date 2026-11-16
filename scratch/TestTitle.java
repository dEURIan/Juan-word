import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestTitle {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("assets/ui/Title.png"));
        int minX = img.getWidth(), maxX = 0, minY = img.getHeight(), maxY = 0;
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
        System.out.println("Title bounds: [" + minX + ", " + minY + ", " + (maxX - minX + 1) + ", " + (maxY - minY + 1) + "] inside " + img.getWidth() + "x" + img.getHeight());
    }
}
