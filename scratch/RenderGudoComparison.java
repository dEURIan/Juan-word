package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderGudoComparison {
    public static void main(String[] args) throws Exception {
        BufferedImage idle = ImageIO.read(new File("assets/gudoidle.png"));
        BufferedImage atk = ImageIO.read(new File("assets/gudoattack.png"));

        BufferedImage preview = new BufferedImage(600, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = preview.createGraphics();
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, 600, 500);

        // Unflipped
        g.drawImage(idle, 0, 0, 300, 500, null);

        // Flipped horizontally
        g.drawImage(idle, 600, 0, -300, 500, null);

        g.dispose();
        ImageIO.write(preview, "png", new File("scratch/gudo_orientation_test.png"));
        System.out.println("Saved scratch/gudo_orientation_test.png");
    }
}
