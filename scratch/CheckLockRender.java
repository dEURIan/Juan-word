import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CheckLockRender {
    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        g2.drawString("🔒", 20, 60);
        g2.dispose();
        
        // Count non-transparent pixels
        int count = 0;
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                if ((img.getRGB(x, y) >>> 24) > 0) count++;
            }
        }
        System.out.println("Non-transparent pixels for lock: " + count);
        ImageIO.write(img, "png", new File("scratch/check_lock_solo.png"));
    }
}
