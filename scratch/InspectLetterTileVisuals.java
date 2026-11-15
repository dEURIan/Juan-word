import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class InspectLetterTileVisuals {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("src/juanword/letters/A.png"));
        System.out.println("Letter A size: " + img.getWidth() + "x" + img.getHeight());

        // Check corner transparency
        int tl = img.getRGB(0, 0);
        int tr = img.getRGB(img.getWidth() - 1, 0);
        int bl = img.getRGB(0, img.getHeight() - 1);
        int br = img.getRGB(img.getWidth() - 1, img.getHeight() - 1);
        int mid = img.getRGB(img.getWidth() / 2, img.getHeight() / 2);

        System.out.printf("Corners: TL=#%08X, TR=#%08X, BL=#%08X, BR=#%08X, Center=#%08X%n", tl, tr, bl, br, mid);

        // Count transparent vs non-transparent pixels
        int opaqueCount = 0;
        int transCount = 0;
        Map<Integer, Integer> colorCounts = new HashMap<>();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                if (a > 10) {
                    opaqueCount++;
                    int rgbNoAlpha = rgb & 0x00FFFFFF;
                    colorCounts.put(rgbNoAlpha, colorCounts.getOrDefault(rgbNoAlpha, 0) + 1);
                } else {
                    transCount++;
                }
            }
        }
        System.out.println("Opaque pixels: " + opaqueCount + ", Transparent: " + transCount);
        System.out.printf("Opaque coverage: %.1f%%%n", (opaqueCount * 100.0) / (opaqueCount + transCount));

        // Top 5 most frequent colors
        System.out.println("Top colors:");
        colorCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(e -> System.out.printf("  Color #%06X (R=%d, G=%d, B=%d): %d pixels%n",
                        e.getKey(), (e.getKey() >> 16) & 0xff, (e.getKey() >> 8) & 0xff, e.getKey() & 0xff, e.getValue()));
    }
}
