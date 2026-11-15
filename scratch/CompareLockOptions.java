import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CompareLockOptions {
    public static void main(String[] args) throws Exception {
        BufferedImage original = ImageIO.read(new File("scratch/dm_lock_crop.png"));
        int w = original.getWidth();
        int h = original.getHeight();
        
        // 1. Original (has yellow circle before lock)
        
        // 2. Option A: Pure lock on silhouette (no circle backing at all, soft shadow for depth)
        BufferedImage optA = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gA = optA.createGraphics();
        // fill with the same dark silhouette color around it
        gA.setColor(new Color(15, 18, 28, 235));
        gA.fillRect(0, 0, w, h);
        gA.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gA.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gA.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        // Subtle drop shadow
        gA.setColor(new Color(0, 0, 0, 180));
        gA.drawString("🔒", w / 2 - 14 + 1, h / 2 + 10 + 1);
        gA.setColor(Color.WHITE);
        gA.drawString("🔒", w / 2 - 14, h / 2 + 10);
        gA.dispose();
        
        // 3. Option B: Subtle dark circle backing, NO yellow rim
        BufferedImage optB = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gB = optB.createGraphics();
        gB.setColor(new Color(15, 18, 28, 235));
        gB.fillRect(0, 0, w, h);
        gB.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gB.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int lockSize = 36;
        int lockX = w / 2 - lockSize / 2;
        int lockY = h / 2 - lockSize / 2;
        gB.setColor(new Color(18, 22, 38, 220));
        gB.fillOval(lockX - 4, lockY - 4, lockSize + 8, lockSize + 8);
        gB.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        gB.setColor(Color.WHITE);
        gB.drawString("🔒", lockX + 6, lockY + 25);
        gB.dispose();
        
        // Put all 3 side-by-side
        BufferedImage comp = new BufferedImage(w * 3 + 40, h + 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gC = comp.createGraphics();
        gC.setColor(new Color(10, 12, 20));
        gC.fillRect(0, 0, comp.getWidth(), comp.getHeight());
        
        gC.drawImage(original, 10, 35, null);
        gC.drawImage(optA, w + 20, 35, null);
        gC.drawImage(optB, w * 2 + 30, 35, null);
        
        gC.setColor(Color.WHITE);
        gC.setFont(new Font("Arial", Font.BOLD, 12));
        gC.drawString("1. Original (Yellow Circle)", 10, 22);
        gC.drawString("2. Pure Lock (No Circle)", w + 20, 22);
        gC.drawString("3. Dark Circle (No Yellow)", w * 2 + 30, 22);
        gC.dispose();
        
        ImageIO.write(comp, "png", new File("scratch/compare_lock_renderings.png"));
        System.out.println("Saved compare_lock_renderings.png");
    }
}
