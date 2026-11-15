import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectCropOriginal {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/crop_original_lock.png"));
        int yellowPixels = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // yellow rim is around r=245, g=175, b=75
                if (r > 200 && g > 150 && b < 100) {
                    yellowPixels++;
                }
            }
        }
        System.out.println("Found yellow pixels in crop: " + yellowPixels);
    }
}
