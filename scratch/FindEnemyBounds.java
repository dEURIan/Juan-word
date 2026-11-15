import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class FindEnemyBounds {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/battle_with_new_alphabet_tiles.png"));
        int minX = 9999, maxX = -1, minY = 9999, maxY = -1;
        for (int y = 50; y < 400; y++) {
            for (int x = 600; x < 950; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (Math.abs(r - 211) < 20 && Math.abs(g - 111) < 20 && b < 30) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        System.out.println("Enemy bounds in BattlePanel: [" + minX + ", " + minY + " to " + maxX + ", " + maxY + "], width=" + (maxX - minX) + ", height=" + (maxY - minY));
    }
}
