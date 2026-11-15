import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectAlphabet {
    public static void main(String[] args) throws Exception {
        int tileThumb = 120;
        int cols = 6;
        int rows = 5; // 6x5 = 30 slots for 26 letters
        BufferedImage grid = new BufferedImage(cols * tileThumb, rows * tileThumb, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = grid.createGraphics();
        g2.setColor(new Color(30, 35, 50));
        g2.fillRect(0, 0, grid.getWidth(), grid.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        System.out.println("Letter index, file, non-empty bounds:");
        for (int i = 1; i <= 26; i++) {
            File f = new File("scratch/alphabet_extract/" + i + ".png");
            if (!f.exists()) continue;
            BufferedImage img = ImageIO.read(f);
            Rectangle b = getNonEmptyBounds(img);
            char letter = (char) ('A' + (i - 1));
            System.out.printf("Letter %c (file %d.png): size=%dx%d, contentBounds=[%d,%d to %d,%d, w=%d, h=%d]%n",
                    letter, i, img.getWidth(), img.getHeight(), b.x, b.y, b.x + b.width, b.y + b.height, b.width, b.height);

            int col = (i - 1) % cols;
            int row = (i - 1) / cols;
            int x = col * tileThumb + 10;
            int y = row * tileThumb + 10;
            int size = tileThumb - 20;

            // Draw thumbnail
            g2.drawImage(img, x, y, size, size, null);

            // Label
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(letter + " (" + i + ")", x + 5, y + size - 5);
        }
        g2.dispose();

        File out = new File("scratch/alphabet_collage.png");
        ImageIO.write(grid, "png", out);
        System.out.println("Saved alphabet collage to: " + out.getAbsolutePath());
    }

    private static Rectangle getNonEmptyBounds(BufferedImage img) {
        int minX = img.getWidth(), maxX = 0;
        int minY = img.getHeight(), maxY = 0;
        for (int y = 0; y < img.getHeight(); y += 4) {
            for (int x = 0; x < img.getWidth(); x += 4) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 10) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (minX > maxX) return new Rectangle(0, 0, img.getWidth(), img.getHeight());
        return new Rectangle(minX, minY, Math.max(1, maxX - minX + 1), Math.max(1, maxY - minY + 1));
    }
}
