import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectMockupLayout {
    public static void main(String[] args) throws Exception {
        BufferedImage mock = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794751758936_3dc55716.jpg"));
        int w = mock.getWidth();
        int h = mock.getHeight();
        System.out.println("Mockup size: " + w + "x" + h);
        
        // Let's sample colors across the bottom half (y from h/2 to h)
        // Left region: San Pedro (grey building with arches)
        // Center region: Clock Tower (tall clock spire)
        // Right region: Durian Museum (faceted teal/green dome)
        
        // Print pixel info along y = (int)(h * 0.75)
        int y = (int)(h * 0.75);
        for (int x = 0; x < w; x += 20) {
            int rgb = mock.getRGB(x, y);
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            System.out.printf("x=%3d, y=%3d: R=%3d G=%3d B=%3d%n", x, y, r, g, b);
        }
    }
}
