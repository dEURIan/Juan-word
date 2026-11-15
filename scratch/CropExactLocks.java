import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CropExactLocks {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/hover_locked_museum_preview.png"));
        // Mt Apo Lock: (459, 147, 42, 42) -> crop 430, 120, 100, 100
        BufferedImage apoLock = img.getSubimage(430, 120, 100, 100);
        ImageIO.write(apoLock, "png", new File("scratch/apo_lock_crop.png"));
        
        // Davao Museum Lock: (438, 385, 44, 44) -> crop 410, 360, 100, 100
        BufferedImage dmLock = img.getSubimage(410, 360, 100, 100);
        ImageIO.write(dmLock, "png", new File("scratch/dm_lock_crop.png"));
        
        // San Pedro Lock: (698, 390, 44, 44) -> crop 670, 360, 100, 100
        BufferedImage spLock = img.getSubimage(670, 360, 100, 100);
        ImageIO.write(spLock, "png", new File("scratch/sp_lock_crop.png"));
        
        System.out.println("Saved apo_lock_crop.png, dm_lock_crop.png, and sp_lock_crop.png");
    }
}
