package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MeasureBorder {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("indigframe.png"));
        int w = img.getWidth();
        int h = img.getHeight();
        System.out.println("Size: " + w + "x" + h);

        int top = 0;
        while (top < h && ((img.getRGB(w / 2, top) >> 24) & 0xFF) > 20) {
            top++;
        }

        int bottom = 0;
        while (bottom < h && ((img.getRGB(w / 2, h - 1 - bottom) >> 24) & 0xFF) > 20) {
            bottom++;
        }

        int left = 0;
        while (left < w && ((img.getRGB(left, h / 2) >> 24) & 0xFF) > 20) {
            left++;
        }

        int right = 0;
        while (right < w && ((img.getRGB(w - 1 - right, h / 2) >> 24) & 0xFF) > 20) {
            right++;
        }

        System.out.printf("Border in 1920x1080: top=%d (%.2f%%), bottom=%d (%.2f%%), left=%d (%.2f%%), right=%d (%.2f%%)\n",
            top, (100.0 * top / h), bottom, (100.0 * bottom / h), left, (100.0 * left / w), right, (100.0 * right / w));

        System.out.printf("In 960x540: top=%.1fpx, bottom=%.1fpx, left=%.1fpx, right=%.1fpx\n",
            top * (540.0 / h), bottom * (540.0 / h), left * (960.0 / w), right * (960.0 / w));
            
        // Check middle 80% to find frame thickness along each edge
        int midTop = 0, midBottom = 0, midLeft = 0, midRight = 0;
        for (int x = (int)(w * 0.1); x < (int)(w * 0.9); x += 20) {
            int t = 0;
            while (t < h && ((img.getRGB(x, t) >> 24) & 0xFF) > 20) t++;
            if (t > midTop) midTop = t;
            int b = 0;
            while (b < h && ((img.getRGB(x, h - 1 - b) >> 24) & 0xFF) > 20) b++;
            if (b > midBottom) midBottom = b;
        }
        for (int y = (int)(h * 0.1); y < (int)(h * 0.9); y += 20) {
            int l = 0;
            while (l < w && ((img.getRGB(l, y) >> 24) & 0xFF) > 20) l++;
            if (l > midLeft) midLeft = l;
            int r = 0;
            while (r < w && ((img.getRGB(w - 1 - r, y) >> 24) & 0xFF) > 20) r++;
            if (r > midRight) midRight = r;
        }
        System.out.printf("Frame Band Thickness in 1920x1080: Top=%d (%.2f%%), Bottom=%d (%.2f%%), Left=%d (%.2f%%), Right=%d (%.2f%%)\n",
            midTop, (100.0 * midTop / h), midBottom, (100.0 * midBottom / h), midLeft, (100.0 * midLeft / w), midRight, (100.0 * midRight / w));
        System.out.printf("In 960x540: Top=%.1fpx, Bottom=%.1fpx, Left=%.1fpx, Right=%.1fpx\n",
            midTop * (540.0 / h), midBottom * (540.0 / h), midLeft * (960.0 / w), midRight * (960.0 / w));
        System.out.printf("In 1280x720: Top=%.1fpx, Bottom=%.1fpx, Left=%.1fpx, Right=%.1fpx\n",
            midTop * (720.0 / h), midBottom * (720.0 / h), midLeft * (1280.0 / w), midRight * (1280.0 / w));
    }
}
