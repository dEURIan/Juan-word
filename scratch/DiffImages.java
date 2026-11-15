import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class DiffImages {
    public static void main(String[] args) throws Exception {
        BufferedImage tp = ImageIO.read(new File("TITLE_PAGE.png"));
        BufferedImage mtapoBg = ImageIO.read(new File("mtapowithbg.png"));
        int diff = 0;
        for (int y = 0; y < 1080; y += 10) {
            for (int x = 0; x < 1920; x += 10) {
                if (tp.getRGB(x, y) != mtapoBg.getRGB(x, y)) {
                    diff++;
                }
            }
        }
        System.out.println("Diff count between TITLE_PAGE and mtapowithbg: " + diff);
    }
}
