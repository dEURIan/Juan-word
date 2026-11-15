import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class FindYellowPixels {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/hover_locked_museum_preview.png"));
        int minX = 9999, maxX = -1, minY = 9999, maxY = -1, count = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // Look for the exact color 245, 175, 75
                if (Math.abs(r - 245) < 15 && Math.abs(g - 175) < 15 && Math.abs(b - 75) < 15) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    count++;
                }
            }
        }
        System.out.println("Yellow count: " + count + ", bounds: [" + minX + "," + minY + " to " + maxX + "," + maxY + "]");
    }
}
