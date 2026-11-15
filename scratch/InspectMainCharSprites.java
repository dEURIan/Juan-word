import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectMainCharSprites {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "maincharacteridle.png",
            "maincharapreattack.png",
            "maincharattack.png",
            "Main Character Idle_1.png"
        };
        
        for (String f : files) {
            File file = new File(f);
            if (!file.exists()) file = new File("src/juanword/" + f);
            if (!file.exists()) {
                System.out.println("Not found: " + f);
                continue;
            }
            BufferedImage img = ImageIO.read(file);
            int minX = 9999, maxX = -1, minY = 9999, maxY = -1;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int alpha = (img.getRGB(x, y) >>> 24);
                    if (alpha > 5) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            System.out.println(f + ": size=" + img.getWidth() + "x" + img.getHeight() 
                + ", content bounds=[" + minX + "," + minY + " to " + maxX + "," + maxY + "]"
                + " (content " + (maxX - minX + 1) + "x" + (maxY - minY + 1) + ")");
        }
    }
}
