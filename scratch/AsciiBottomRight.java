import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class AsciiBottomRight {
    public static void main(String[] args) throws Exception {
        BufferedImage imgA = ImageIO.read(new File("src/juanword/letters/A.png"));
        int w = imgA.getWidth();
        int h = imgA.getHeight();

        System.out.println("--- Bottom Right of A.png (sampled every 8 pixels) ---");
        for (int y = (int)(h * 0.72); y < (int)(h * 0.95); y += 8) {
            StringBuilder sb = new StringBuilder();
            for (int x = (int)(w * 0.72); x < (int)(w * 0.95); x += 8) {
                int rgb = imgA.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                sb.append(r < 100 ? "#" : ".");
            }
            System.out.println(sb.toString());
        }

        BufferedImage imgZ = ImageIO.read(new File("src/juanword/letters/Z.png"));
        System.out.println("--- Bottom Right of Z.png (sampled every 8 pixels) ---");
        for (int y = (int)(h * 0.72); y < (int)(h * 0.95); y += 8) {
            StringBuilder sb = new StringBuilder();
            for (int x = (int)(w * 0.72); x < (int)(w * 0.95); x += 8) {
                int rgb = imgZ.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                sb.append(r < 100 ? "#" : ".");
            }
            System.out.println(sb.toString());
        }
    }
}
