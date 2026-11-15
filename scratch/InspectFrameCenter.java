import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectFrameCenter {
    public static void main(String[] args) throws Exception {
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        int w = frame.getWidth();
        int h = frame.getHeight();
        // Check center
        int centerAlpha = (frame.getRGB(w/2, h/2) >> 24) & 0xff;
        System.out.println("Frame center alpha (at " + w/2 + "," + h/2 + "): " + centerAlpha);

        // Find inner transparent box
        int topBorder = 0, bottomBorder = 0, leftBorder = 0, rightBorder = 0;
        for (int y = 0; y < h; y++) {
            if (((frame.getRGB(w/2, y) >> 24) & 0xff) > 10) topBorder = y;
            else break;
        }
        for (int y = h - 1; y >= 0; y--) {
            if (((frame.getRGB(w/2, y) >> 24) & 0xff) > 10) bottomBorder = h - 1 - y;
            else break;
        }
        for (int x = 0; x < w; x++) {
            if (((frame.getRGB(x, h/2) >> 24) & 0xff) > 10) leftBorder = x;
            else break;
        }
        for (int x = w - 1; x >= 0; x--) {
            if (((frame.getRGB(x, h/2) >> 24) & 0xff) > 10) rightBorder = w - 1 - x;
            else break;
        }
        System.out.printf("Frame border thickness: top=%d, bottom=%d, left=%d, right=%d%n",
            topBorder, bottomBorder, leftBorder, rightBorder);
    }
}
