import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ComparePreview {
    public static void main(String[] args) throws Exception {
        BufferedImage s0 = ImageIO.read(new File("scratch/single_line_banner_0.png"));
        BufferedImage t0 = ImageIO.read(new File("scratch/two_line_banner_0.png"));
        
        // Crop the banner area from each: x: 250 to 710, y: 15 to 125
        BufferedImage cropS = s0.getSubimage(250, 15, 460, 100);
        BufferedImage cropT = t0.getSubimage(250, 15, 460, 100);
        
        BufferedImage comp = new BufferedImage(460, 220, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = comp.createGraphics();
        g.setColor(new Color(20, 24, 38));
        g.fillRect(0, 0, 460, 220);
        g.drawImage(cropS, 0, 0, null);
        g.drawImage(cropT, 0, 110, null);
        
        g.setColor(Color.YELLOW);
        g.drawString("OPTION A: Single-Line Banner", 10, 18);
        g.drawString("OPTION B: Two-Line Banner", 10, 128);
        g.dispose();
        
        ImageIO.write(comp, "png", new File("scratch/banner_options_comparison.png"));
        System.out.println("Saved banner_options_comparison.png");
    }
}
