import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.ImageLoader;
import juanword.ui.Landmark;

public class TestApoRenderOptions {
    public static void main(String[] args) throws Exception {
        BufferedImage sky = ImageLoader.load("bg.png");
        BufferedImage mtApo = ImageLoader.load("mtapo.png");
        BufferedImage silMtApo = ImageLoader.createSilhouette(mtApo, new Color(18, 24, 36, 245));
        BufferedImage frame = ImageLoader.load("indigframe.png");

        int w = 960, h = 540;

        // Option A: Silhouette when locked
        BufferedImage optA = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gA = optA.createGraphics();
        gA.drawImage(sky, 0, 0, w, h, null);
        gA.drawImage(silMtApo, 0, 0, w, h, null);
        drawLock(gA, w / 2, (int) (h * 0.28));
        gA.drawImage(frame, 0, 0, w, h, null);
        gA.dispose();
        ImageIO.write(optA, "PNG", new File("scratch/apo_opt_a_silhouette.png"));

        // Option B: Regular artwork with summit lock
        BufferedImage optB = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gB = optB.createGraphics();
        gB.drawImage(sky, 0, 0, w, h, null);
        gB.drawImage(mtApo, 0, 0, w, h, null);
        drawLock(gB, w / 2, (int) (h * 0.28));
        gB.drawImage(frame, 0, 0, w, h, null);
        gB.dispose();
        ImageIO.write(optB, "PNG", new File("scratch/apo_opt_b_normal_with_lock.png"));

        System.out.println("Generated Option A and Option B");
    }

    private static void drawLock(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(cx - 16, cy - 16, 32, 32, 10, 10);
        g2.setColor(new Color(230, 235, 245));
        g2.setStroke(new BasicStroke(2.8f));
        g2.drawArc(cx - 6, cy - 11, 12, 14, 0, 180);
        g2.setColor(new Color(235, 185, 50));
        g2.fillRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);
        g2.setColor(new Color(175, 125, 20));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);
        g2.setColor(new Color(40, 30, 20));
        g2.fillOval(cx - 2, cy + 2, 4, 4);
        g2.fillRect(cx - 1, cy + 4, 2, 4);
    }
}
