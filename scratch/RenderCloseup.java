import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class RenderCloseup {
    public static void main(String[] args) throws Exception {
        BufferedImage imgA = ImageIO.read(new File("src/juanword/letters/A.png"));
        BufferedImage imgB = ImageIO.read(new File("src/juanword/letters/B.png"));
        BufferedImage imgZ = ImageIO.read(new File("src/juanword/letters/Z.png"));

        BufferedImage composite = new BufferedImage(600 * 3 + 40, 630 + 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = composite.createGraphics();
        g.setColor(new Color(24, 30, 48));
        g.fillRect(0, 0, composite.getWidth(), composite.getHeight());

        g.drawImage(imgA, 10, 20, null);
        g.drawImage(imgB, 620, 20, null);
        g.drawImage(imgZ, 1230, 20, null);

        g.dispose();
        ImageIO.write(composite, "png", new File("scratch/closeup_abz.png"));
        ImageIO.write(composite, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/closeup_abz.png"));
        System.out.println("Saved closeup_abz.png");
    }
}
