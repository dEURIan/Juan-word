import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestMapCompositeMatch {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage sanpedro = ImageIO.read(new File("sanpedro.png"));
        BufferedImage clocktower = ImageIO.read(new File("clocktower.png"));
        BufferedImage durianmuseum = ImageIO.read(new File("durianmuseum.png"));

        // Render at 960x540 (half of 1920x1080)
        int w = 960;
        int h = 540;

        BufferedImage composite = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = composite.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Background sky
        g2.drawImage(bg, 0, 0, w, h, null);

        // 2. Mt Apo
        g2.drawImage(mtapo, 0, 0, w, h, null);

        // 3. San Pedro: natively 1920x1080. If drawn directly at 0, 0, w, h:
        // Let's see: sanpedro is on the left
        g2.drawImage(sanpedro, 0, 0, w, h, null);

        // 4. Clock Tower: visible bounds are [269, 96 to 884, 1839], size 616x1744 in 1080x1920
        // In the mockup: clock tower is centered horizontally (x around 480 in 960 width).
        // Mockup: tower width is about 120-140px, tower top reaches y ~ 140, tower bottom touches bottom (h=540).
        // Let's crop tight clocktower:
        BufferedImage ctTight = clocktower.getSubimage(269, 96, 616, 1744);
        int ctH = (int)(h * 0.72); // about 388px high
        int ctW = (int)(ctH * (616.0 / 1744.0)); // about 137px wide
        int ctX = (w - ctW) / 2; // centered
        int ctY = h - ctH - 12; // resting at bottom above frame
        g2.drawImage(ctTight, ctX, ctY, ctW, ctH, null);

        // 5. Durian Museum: visible bounds are [0, 300 to 1079, 1919], size 1080x1620
        // In the mockup: durian dome is on the right side (x from ~650 to ~950).
        BufferedImage dmTight = durianmuseum.getSubimage(0, 300, 1080, 1620);
        int dmH = (int)(h * 0.52); // about 280px high
        int dmW = (int)(dmH * (1080.0 / 1620.0)); // about 280 * 0.667 = 187px, or wider?
        // Wait, durianmuseum width in the mockup is wider! Let's check aspect ratio of durian museum dome
        int dmX = w - dmW - 20;
        int dmY = h - dmH - 12;
        g2.drawImage(dmTight, dmX, dmY, dmW, dmH, null);

        // 6. Frame on top
        g2.drawImage(frame, 0, 0, w, h, null);

        g2.dispose();

        File out = new File("scratch/test_map_composite_match.png");
        ImageIO.write(composite, "png", out);
        System.out.println("Saved to " + out.getAbsolutePath());
    }
}
