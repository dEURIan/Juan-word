import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TestLetterCheck {
    public static void main(String[] args) throws Exception {
        BufferedImage imgA = ImageIO.read(new File("src/juanword/letters/a.png"));
        BufferedImage imgB = ImageIO.read(new File("src/juanword/letters/b.png"));
        System.out.println("a.png size: " + imgA.getWidth() + "x" + imgA.getHeight());
        System.out.println("b.png size: " + imgB.getWidth() + "x" + imgB.getHeight());
    }
}
