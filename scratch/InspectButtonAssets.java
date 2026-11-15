import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class InspectButtonAssets {
    public static void main(String[] args) throws Exception {
        String base = "/Users/euri/Downloads/Juan Word Assets/";
        String[] files = {"startbtn.png", "newgamebtn.png", "continuebtn.png", "settingbtn.png", "exitbtn.png"};
        for (String f : files) {
            File file = new File(base + f);
            BufferedImage img = ImageIO.read(file);
            System.out.println(f + " -> " + img.getWidth() + "x" + img.getHeight() + ", hasAlpha=" + img.getColorModel().hasAlpha());
        }
    }
}
