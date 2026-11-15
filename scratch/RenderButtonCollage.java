import javax.imageio.ImageIO;
import java.io.File;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderButtonCollage {
    public static void main(String[] args) throws Exception {
        String base = "/Users/euri/Downloads/Juan Word Assets/";
        String[] files = {"startbtn.png", "newgamebtn.png", "continuebtn.png", "settingbtn.png", "exitbtn.png"};
        
        BufferedImage collage = new BufferedImage(300, 60 * files.length + 20 * (files.length + 1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = collage.createGraphics();
        g2.setColor(new Color(20, 24, 45));
        g2.fillRect(0, 0, collage.getWidth(), collage.getHeight());
        
        int y = 20;
        for (String f : files) {
            BufferedImage img = ImageIO.read(new File(base + f));
            g2.drawImage(img, (collage.getWidth() - img.getWidth()) / 2, y, null);
            y += img.getHeight() + 20;
        }
        g2.dispose();
        
        File out = new File("scratch/button_collage.png");
        ImageIO.write(collage, "png", out);
        System.out.println("Collage written to " + out.getAbsolutePath());
    }
}
