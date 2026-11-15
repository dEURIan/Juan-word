import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CheckCompPixels {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/banner_options_comparison.png"));
        System.out.println("Comparison image size: " + img.getWidth() + "x" + img.getHeight());
    }
}
