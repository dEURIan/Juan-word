import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectTileContent {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("src/juanword/letters/A.png"));
        int w = img.getWidth();
        int h = img.getHeight();

        // Check if there are dark pixels in the bottom right corner (where Scrabble/Bookworm points usually sit)
        int brDarkPixels = 0;
        int centerDarkPixels = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (r < 80 && g < 80 && b < 80) { // Dark pixel
                    if (x > w * 0.7 && y > h * 0.7) {
                        brDarkPixels++;
                    } else if (x > w * 0.2 && x < w * 0.8 && y > h * 0.2 && y < h * 0.8) {
                        centerDarkPixels++;
                    }
                }
            }
        }
        System.out.println("Letter A dark pixels - Center (letter): " + centerDarkPixels + ", Bottom-Right (subscript points?): " + brDarkPixels);

        // Check Letter B
        BufferedImage imgB = ImageIO.read(new File("src/juanword/letters/B.png"));
        int brB = 0;
        for (int y = (int)(h * 0.7); y < h; y++) {
            for (int x = (int)(w * 0.7); x < w; x++) {
                int rgb = imgB.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (r < 80 && g < 80 && b < 80) brB++;
            }
        }
        System.out.println("Letter B dark pixels - Bottom-Right: " + brB);
    }
}
