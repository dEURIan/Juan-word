import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectMenuComposite {
    public static void main(String[] args) throws Exception {
        BufferedImage preview = ImageIO.read(new File("scratch/menu_composite_preview.png"));
        int minX = preview.getWidth(), maxX = 0, minY = preview.getHeight(), maxY = 0;
        int count = 0;
        for (int y = 0; y < 350; y++) {
            for (int x = 200; x < 760; x++) {
                int rgb = preview.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                if (r > 180 && g > 120 && b < 100) {
                    count++;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        System.out.printf("Title text detected in composite preview: count=%d, bounds=[%d,%d to %d,%d], size=%dx%d%n",
            count, minX, minY, maxX, maxY, (maxX - minX + 1), (maxY - minY + 1));
    }
}
