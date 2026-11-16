package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CropDurianMuseum {
    public static void main(String[] args) throws Exception {
        File origFile = new File("assets/maps/durianmuseum.png");
        BufferedImage orig = ImageIO.read(origFile);
        int w = orig.getWidth(); // 1080
        int newH = 1530; // Crop off the 390px of blank pavement floor

        BufferedImage cropped = orig.getSubimage(0, 0, w, newH);

        // Write to assets/maps/durianmuseum.png
        ImageIO.write(cropped, "PNG", origFile);
        System.out.println("Cropped assets/maps/durianmuseum.png: 1080x" + newH);

        // Also update any copy in build/classes if it exists
        File b1 = new File("build/classes/assets/maps/durianmuseum.png");
        if (b1.exists()) ImageIO.write(cropped, "PNG", b1);
        File b2 = new File("build/classes/juanword/durianmuseum.png");
        if (b2.exists()) ImageIO.write(cropped, "PNG", b2);
    }
}
