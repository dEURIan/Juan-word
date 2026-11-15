import juanword.JuanWord;
import java.awt.Image;

public class TestLoadLetterAssets {
    public static void main(String[] args) {
        JuanWord app = new JuanWord();
        for (char c = 'A'; c <= 'Z'; c++) {
            Image img = app.loadGameImage("letters/" + c + ".png");
            assert img != null : "Could not load letter " + c;
            assert img.getWidth(null) > 0 : "Letter " + c + " has invalid width: " + img.getWidth(null);
        }
        System.out.println("✓ All 26 letters (A-Z) loaded successfully via app.loadGameImage!");
        System.exit(0);
    }
}
