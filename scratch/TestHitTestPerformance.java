import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestHitTestPerformance {
    public static void main(String[] args) throws Exception {
        BufferedImage ctTight = ImageIO.read(new File("clocktower.png")).getSubimage(269, 96, 616, 1744);
        BufferedImage dmTight = ImageIO.read(new File("durianmuseum.png")).getSubimage(0, 300, 1080, 1620);
        BufferedImage spTight = ImageIO.read(new File("sanpedro.png")).getSubimage(0, 201, 1612, 865);

        int w = 960, h = 540;
        int ctH = (int)(h * 0.76);
        int ctW = (int)(ctH * (ctTight.getWidth() / (double)ctTight.getHeight()));
        int ctX = (w - ctW) / 2;
        int ctY = h - ctH - 18;

        long start = System.nanoTime();
        int hits = 0;
        for (int y = 0; y < h; y += 5) {
            for (int x = 0; x < w; x += 5) {
                // Test clocktower
                int lx = (int)((x - ctX) * (double)ctTight.getWidth() / ctW);
                int ly = (int)((y - ctY) * (double)ctTight.getHeight() / ctH);
                if (lx >= 0 && lx < ctTight.getWidth() && ly >= 0 && ly < ctTight.getHeight()) {
                    if (((ctTight.getRGB(lx, ly) >> 24) & 0xff) > 20) hits++;
                }
            }
        }
        long duration = System.nanoTime() - start;
        System.out.printf("Checked ~20,000 points in %.2f ms (%d hits)%n", duration / 1_000_000.0, hits);
    }
}
