import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestLockRender {
    public static void main(String[] args) throws Exception {
        BufferedImage base = ImageIO.read(new File("scratch/test_silhouette.png"));
        int w = base.getWidth();
        int h = base.getHeight();
        
        // Option 1: Just remove the yellow circle outline (keep dark backing)
        BufferedImage opt1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = opt1.createGraphics();
        g1.drawImage(base, 0, 0, null);
        int lockX = w / 2 - 18;
        int lockY = h / 2 - 18;
        int lockSize = 36;
        g1.setColor(new Color(18, 22, 38, 220));
        g1.fillOval(lockX - 4, lockY - 4, lockSize + 8, lockSize + 8);
        g1.setColor(Color.WHITE);
        g1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        g1.drawString("🔒", lockX + 2, lockY + 26);
        g1.dispose();
        ImageIO.write(opt1, "png", new File("scratch/lock_opt1_dark_circle_no_yellow.png"));
        
        // Option 2: No circle at all (pure lock on silhouette)
        BufferedImage opt2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = opt2.createGraphics();
        g2.drawImage(base, 0, 0, null);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        // Drop shadow for crisp contrast against any background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString("🔒", lockX + 1, lockY + 29);
        g2.setColor(Color.WHITE);
        g2.drawString("🔒", lockX, lockY + 28);
        g2.dispose();
        ImageIO.write(opt2, "png", new File("scratch/lock_opt2_no_circle_at_all.png"));
        
        System.out.println("Generated lock options.");
    }
}
