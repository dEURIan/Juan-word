import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CropAlphabet {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/juanword/letters");
        dir.mkdirs();

        int maxW = 0, maxH = 0;
        Rectangle[] bboxes = new Rectangle[27];

        for (int i = 1; i <= 26; i++) {
            File f = new File("scratch/alphabet_extract/" + i + ".png");
            BufferedImage img = ImageIO.read(f);
            Rectangle b = getExactBBox(img);
            bboxes[i] = b;
            if (b.width > maxW) maxW = b.width;
            if (b.height > maxH) maxH = b.height;
            char letter = (char) ('A' + (i - 1));
            System.out.printf("Letter %c (%d.png): bbox=[%d, %d, %d, %d]%n", letter, i, b.x, b.y, b.width, b.height);
        }

        System.out.printf("Max content dimensions: w=%d, h=%d%n", maxW, maxH);

        // Save tight cropped images and also normalized square images in src/juanword/letters/
        for (int i = 1; i <= 26; i++) {
            File f = new File("scratch/alphabet_extract/" + i + ".png");
            BufferedImage img = ImageIO.read(f);
            Rectangle b = bboxes[i];
            BufferedImage cropped = img.getSubimage(b.x, b.y, b.width, b.height);
            char letter = (char) ('A' + (i - 1));
            ImageIO.write(cropped, "png", new File("src/juanword/letters/" + letter + ".png"));
            ImageIO.write(cropped, "png", new File("src/juanword/letters/" + Character.toLowerCase(letter) + ".png"));
        }
        System.out.println("Saved cropped letter assets A-Z in src/juanword/letters/");
    }

    private static Rectangle getExactBBox(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w, maxX = 0;
        int minY = h, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 5) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (minX > maxX) return new Rectangle(0, 0, w, h);
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
