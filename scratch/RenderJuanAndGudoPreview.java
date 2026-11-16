package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderJuanAndGudoPreview {
    public static void main(String[] args) throws Exception {
        BufferedImage juan = ImageIO.read(new File("assets/maincharacteridle.png"));
        BufferedImage juanAtk = ImageIO.read(new File("assets/maincharattack.png"));
        BufferedImage gudo = ImageIO.read(new File("assets/gudoidle.png"));
        BufferedImage gudoAtk = ImageIO.read(new File("assets/gudoattack.png"));

        BufferedImage comp = new BufferedImage(1200, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = comp.createGraphics();
        g.setColor(new Color(20, 25, 45));
        g.fillRect(0, 0, 1200, 500);

        g.drawImage(juan, 0, 0, 300, 500, null);
        g.drawImage(juanAtk, 300, 0, 300, 500, null);
        g.drawImage(gudo, 600, 0, 300, 500, null);
        g.drawImage(gudoAtk, 900, 0, 300, 500, null);

        g.dispose();
        ImageIO.write(comp, "png", new File("scratch/sprites_preview.png"));
        System.out.println("Preview saved to scratch/sprites_preview.png");
    }
}
