import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestSilhouette {
    public static void main(String[] args) throws Exception {
        BufferedImage src = ImageIO.read(new File("Blue Building.png"));
        BufferedImage sil = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sil.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(new Color(15, 18, 28, 240)); // or Color.BLACK
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();

        ImageIO.write(sil, "png", new File("scratch/test_silhouette.png"));
        System.out.println("Silhouette created successfully! Size: " + sil.getWidth() + "x" + sil.getHeight());
    }
}
