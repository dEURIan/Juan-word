import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectMapAssets {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "sanpedro.png", "clocktower.png", "durianmuseum.png",
            "sanpedrowithbg.png", "clocktowerwithbg.png", "durianmuseumwithbg.png",
            "/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794751758936_3dc55716.jpg"
        };
        for (String fpath : files) {
            File f = new File(fpath);
            if (!f.exists()) {
                System.out.println("NOT FOUND: " + fpath);
                continue;
            }
            BufferedImage img = ImageIO.read(f);
            int minX = img.getWidth(), maxX = 0, minY = img.getHeight(), maxY = 0;
            int nonTrans = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int a = (img.getRGB(x, y) >> 24) & 0xff;
                    if (a > 10) {
                        nonTrans++;
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.printf("%s: %dx%d, nonTrans=%d, bounds=[%d,%d to %d,%d] (size %dx%d)%n",
                f.getName(), img.getWidth(), img.getHeight(), nonTrans,
                minX, minY, maxX, maxY, (maxX - minX + 1), (maxY - minY + 1));
        }
    }
}
