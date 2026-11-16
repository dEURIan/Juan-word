import java.awt.image.BufferedImage;
import juanword.ui.ImageLoader;

public class TestLoadLetterAssets {
    public static void main(String[] args) {
        for (char c = 'A'; c <= 'Z'; c++) {
            BufferedImage img = ImageLoader.load("letters/" + c + ".png");
            assert img != null : "Could not load letter " + c;
            assert img.getWidth() > 0 : "Letter " + c + " has invalid width: " + img.getWidth();
        }
        System.out.println("✓ All 26 letters (A-Z) loaded successfully via ImageLoader.load!");
        System.exit(0);
    }
}
