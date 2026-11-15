import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestLandmarkCropping {
    public static void main(String[] args) throws Exception {
        BufferedImage mock = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794751758936_3dc55716.jpg"));
        BufferedImage sanpedro = ImageIO.read(new File("sanpedro.png"));
        BufferedImage clocktower = ImageIO.read(new File("clocktower.png"));
        BufferedImage durianmuseum = ImageIO.read(new File("durianmuseum.png"));

        System.out.println("sanpedro raw: " + sanpedro.getWidth() + "x" + sanpedro.getHeight());
        System.out.println("clocktower raw: " + clocktower.getWidth() + "x" + clocktower.getHeight());
        System.out.println("durianmuseum raw: " + durianmuseum.getWidth() + "x" + durianmuseum.getHeight());
        
        // Find bounding box of non-transparent pixels for clocktower and durianmuseum
        int ctMinX = clocktower.getWidth(), ctMaxX = 0, ctMinY = clocktower.getHeight(), ctMaxY = 0;
        for (int y = 0; y < clocktower.getHeight(); y++) {
            for (int x = 0; x < clocktower.getWidth(); x++) {
                if (((clocktower.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (x < ctMinX) ctMinX = x;
                    if (x > ctMaxX) ctMaxX = x;
                    if (y < ctMinY) ctMinY = y;
                    if (y > ctMaxY) ctMaxY = y;
                }
            }
        }
        System.out.printf("Clock Tower non-trans: [%d,%d to %d,%d], size %dx%d%n",
            ctMinX, ctMinY, ctMaxX, ctMaxY, (ctMaxX - ctMinX + 1), (ctMaxY - ctMinY + 1));

        int dmMinX = durianmuseum.getWidth(), dmMaxX = 0, dmMinY = durianmuseum.getHeight(), dmMaxY = 0;
        for (int y = 0; y < durianmuseum.getHeight(); y++) {
            for (int x = 0; x < durianmuseum.getWidth(); x++) {
                if (((durianmuseum.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (x < dmMinX) dmMinX = x;
                    if (x > dmMaxX) dmMaxX = x;
                    if (y < dmMinY) dmMinY = y;
                    if (y > dmMaxY) dmMaxY = y;
                }
            }
        }
        System.out.printf("Durian Museum non-trans: [%d,%d to %d,%d], size %dx%d%n",
            dmMinX, dmMinY, dmMaxX, dmMaxY, (dmMaxX - dmMinX + 1), (dmMaxY - dmMinY + 1));

        int spMinX = sanpedro.getWidth(), spMaxX = 0, spMinY = sanpedro.getHeight(), spMaxY = 0;
        for (int y = 0; y < sanpedro.getHeight(); y++) {
            for (int x = 0; x < sanpedro.getWidth(); x++) {
                if (((sanpedro.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (x < spMinX) spMinX = x;
                    if (x > spMaxX) spMaxX = x;
                    if (y < spMinY) spMinY = y;
                    if (y > spMaxY) spMaxY = y;
                }
            }
        }
        System.out.printf("San Pedro non-trans: [%d,%d to %d,%d], size %dx%d%n",
            spMinX, spMinY, spMaxX, spMaxY, (spMaxX - spMinX + 1), (spMaxY - spMinY + 1));
    }
}
