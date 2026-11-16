package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class InspectImage {
    public static void main(String[] args) throws Exception {
        File file = new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794773907294_f51ec9a7.jpg");
        BufferedImage img = ImageIO.read(file);
        int w = img.getWidth(), h = img.getHeight();
        System.out.println("Image size: " + w + "x" + h);

        int minY = -1, maxY = -1, minX = -1, maxX = -1;
        for (int y = 0; y < 150; y++) {
            for (int x = 0; x < 300; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Back button color ~ (77, 166, 197)
                if (g > 130 && b > 150 && r < 100) {
                    if (minY == -1) minY = y;
                    maxY = y;
                    if (minX == -1 || x < minX) minX = x;
                    if (maxX == -1 || x > maxX) maxX = x;
                }
            }
        }
        System.out.printf("Back button in image: X=[%d..%d], Y=[%d..%d]\n", minX, maxX, minY, maxY);

        // Find border top
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        int frameH = frame.getHeight();
        int frameW = frame.getWidth();
        // Scale to image size
        double scaleY = (double) h / frameH;
        double scaleX = (double) w / frameW;
        System.out.printf("Frame border at this size: Top=%.1fpx, Bottom=%.1fpx from bottom, Left=%.1fpx, Right=%.1fpx\n",
            35 * scaleY, 45 * scaleY, 48 * scaleX, 48 * scaleX);

        // Find bottom tile
        int bottomTileY = -1;
        for (int y = h - 1; y > h - 150; y--) {
            for (int x = w / 4; x < 3 * w / 4; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (r > 230 && g > 230 && b > 210) {
                    if (bottomTileY == -1) bottomTileY = y;
                }
            }
        }
        System.out.printf("Bottom tile lowest Y: %d, distance from bottom: %dpx\n", bottomTileY, (h - 1 - bottomTileY));
    }
}
