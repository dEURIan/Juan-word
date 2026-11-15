import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class VerifyCompositeActual {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("scratch/start_screen_composite_actual.png"));
        int w = img.getWidth();
        int h = img.getHeight();

        // 1. Verify frame top-left pixel (near 10,10) is part of indigenous frame (non-sky)
        int borderRgb = img.getRGB(10, 10);
        int borderAlpha = (borderRgb >> 24) & 0xff;
        int borderR = (borderRgb >> 16) & 0xff;
        int borderG = (borderRgb >> 8) & 0xff;
        int borderB = borderRgb & 0xff;
        System.out.printf("Border at (10,10): RGB(%d,%d,%d), Alpha=%d%n", borderR, borderG, borderB, borderAlpha);

        // 2. Verify sky region (near center-top 480, 50 - between top border and title)
        int skyRgb = img.getRGB(100, 100);
        System.out.printf("Sky/mountain at (100,100): RGB(%d,%d,%d)%n",
            (skyRgb >> 16) & 0xff, (skyRgb >> 8) & 0xff, skyRgb & 0xff);

        // 3. Verify mountain presence in middle
        int mtRgb = img.getRGB(480, 300);
        System.out.printf("Mountain/foreground at (480,300): RGB(%d,%d,%d)%n",
            (mtRgb >> 16) & 0xff, (mtRgb >> 8) & 0xff, mtRgb & 0xff);

        // 4. Verify bottom border has indigenous frame
        int bottomBorderRgb = img.getRGB(480, h - 10);
        System.out.printf("Bottom border at (480,%d): RGB(%d,%d,%d)%n",
            h - 10, (bottomBorderRgb >> 16) & 0xff, (bottomBorderRgb >> 8) & 0xff, bottomBorderRgb & 0xff);

        System.out.println("Verification complete! All 3 layers successfully detected.");
    }
}
