import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestLockScreenRender {
    public static void main(String[] args) throws Exception {
        // Let's load the full resting screen
        BufferedImage full = ImageIO.read(new File("scratch/test_new_map_arrangement.png"));
        int w = full.getWidth();
        int h = full.getHeight();
        
        // Let's test on Davao Museum silhouette (center)
        // Position of museum lock in test_new_map_arrangement:
        // lockX around 480, lockY around 350
        
        // Let's create Option A: Pure Lock (no circle at all, with soft shadow and crisp lock)
        BufferedImage imgA = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gA = imgA.createGraphics();
        gA.drawImage(full, 0, 0, null);
        
        // Re-paint over Davao Museum lock area with pure lock (no circle)
        // Let's crop just Davao Museum area
        BufferedImage dmCrop = imgA.getSubimage(400, 280, 200, 180);
        ImageIO.write(dmCrop, "png", new File("scratch/crop_original_lock.png"));
        
        System.out.println("Original lock cropped.");
    }
}
