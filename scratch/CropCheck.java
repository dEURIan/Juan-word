import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CropCheck {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/hover_locked_museum_preview.png"));
        // crop the banner (top) and the lock on museum
        BufferedImage bannerCrop = img.getSubimage(200, 50, 560, 80);
        ImageIO.write(bannerCrop, "png", new File("scratch/banner_crop.png"));
        
        BufferedImage lockCrop = img.getSubimage(450, 310, 80, 80);
        ImageIO.write(lockCrop, "png", new File("scratch/lock_crop.png"));
        System.out.println("Saved banner_crop.png and lock_crop.png");
    }
}
