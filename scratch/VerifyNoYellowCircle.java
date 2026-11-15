import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class VerifyNoYellowCircle {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/map_resting_no_yellow_circle.png"));
        // Check Davao Museum lock area: x 430 to 470, y 370 to 410
        int yellowInLock = 0;
        for (int y = 370; y < 410; y++) {
            for (int x = 430; x < 470; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // check for yellow circle color (245, 175, 75)
                if (Math.abs(r - 245) < 15 && Math.abs(g - 175) < 15 && Math.abs(b - 75) < 15) {
                    yellowInLock++;
                }
            }
        }
        System.out.println("Yellow pixels in museum lock area: " + yellowInLock);
        assert yellowInLock == 0 : "Must have 0 yellow circle pixels in lock area";
        System.out.println("✓ VERIFIED: Zero yellow circle pixels in lock area!");
    }
}
