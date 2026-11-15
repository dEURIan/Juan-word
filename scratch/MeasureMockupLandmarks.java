import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class MeasureMockupLandmarks {
    public static void main(String[] args) throws Exception {
        BufferedImage mock = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794751758936_3dc55716.jpg"));
        int w = mock.getWidth(); // 480
        int h = mock.getHeight(); // 273

        // Clock tower top: find highest pixel of golden spire in x range [200, 280]
        int ctTopY = h;
        int ctClockY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 200; x < 280; x++) {
                int rgb = mock.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Golden spire: high R and G, lower B
                if (r > 180 && g > 150 && b > 80 && b < 140) {
                    if (y < ctTopY) ctTopY = y;
                }
                // Clock face: white circle
                if (r > 200 && g > 200 && b > 200) {
                    if (y > ctClockY && y < h * 0.8) ctClockY = y;
                }
            }
        }

        // Durian museum dome: find highest pixel of teal facet in x range [300, 470]
        int dmTopY = h;
        int dmLeftX = w, dmRightX = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 300; x < w - 10; x++) {
                int rgb = mock.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Teal/cyan: g > 100, b > 100, r < 80
                if (g > 100 && b > 100 && r < 100) {
                    if (y < dmTopY) dmTopY = y;
                    if (x < dmLeftX) dmLeftX = x;
                    if (x > dmRightX) dmRightX = x;
                }
            }
        }

        // San Pedro cross/spire on top of roof: find in x range [100, 180]
        int spCrossTopY = h;
        for (int y = 0; y < h; y++) {
            for (int x = 120; x < 170; x++) {
                int rgb = mock.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Cross spire is grey/concrete
                if (Math.abs(r - g) < 15 && Math.abs(g - b) < 15 && r > 90 && r < 160) {
                    if (y < spCrossTopY) spCrossTopY = y;
                }
            }
        }

        System.out.printf("In 480x273 mockup:%n");
        System.out.printf("Clock Tower top spire: y=%d (%.1f%% of height)%n", ctTopY, (ctTopY * 100.0 / h));
        System.out.printf("Durian Museum top: y=%d (%.1f%% of height), X range=[%d, %d] (%.1f%% to %.1f%%)%n",
            dmTopY, (dmTopY * 100.0 / h), dmLeftX, dmRightX, (dmLeftX * 100.0 / w), (dmRightX * 100.0 / w));
        System.out.printf("San Pedro spire top: y=%d (%.1f%% of height)%n", spCrossTopY, (spCrossTopY * 100.0 / h));
    }
}
