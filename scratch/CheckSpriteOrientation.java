package scratch;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckSpriteOrientation {
    public static void main(String[] args) throws Exception {
        BufferedImage juan = ImageIO.read(new File("assets/maincharacteridle.png"));
        BufferedImage gudo = ImageIO.read(new File("assets/gudoidle.png"));
        BufferedImage gudoAtk = ImageIO.read(new File("assets/gudoattack.png"));

        System.out.println("Juan: " + juan.getWidth() + "x" + juan.getHeight());
        System.out.println("Gudo: " + gudo.getWidth() + "x" + gudo.getHeight());
        System.out.println("Gudo Attack: " + gudoAtk.getWidth() + "x" + gudoAtk.getHeight());
    }
}
