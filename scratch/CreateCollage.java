import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CreateCollage {
    public static void main(String[] args) throws Exception {
        BufferedImage img1 = ImageIO.read(new File("scratch/map_resting_screen.png"));
        BufferedImage img2 = ImageIO.read(new File("scratch/map_expanding_50pct.png"));
        BufferedImage img3 = ImageIO.read(new File("scratch/map_expanding_95pct.png"));
        BufferedImage img4 = ImageIO.read(new File("scratch/battle_centered_tiles_and_bg.png"));

        int w = 960 / 2;
        int h = 640 / 2;
        BufferedImage collage = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = collage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(img1, 0, 0, w, h, null);
        g.drawImage(img2, w, 0, w, h, null);
        g.drawImage(img3, 0, h, w, h, null);
        g.drawImage(img4, w, h, w, h, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("1. Resting Map Selection", 15, 25);
        g.drawString("2. Expanding (50% - Landmark Zooms, Rest Fades)", w + 15, 25);
        g.drawString("3. Fully Expanded (95% - Matches Battle BG)", 15, h + 25);
        g.drawString("4. Battle Screen (Centered 52x52 Tiles & BG)", w + 15, h + 25);

        g.dispose();
        ImageIO.write(collage, "PNG", new File("scratch/map_to_battle_progression.png"));
        ImageIO.write(collage, "PNG", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/map_to_battle_progression.png"));
        System.out.println("Collage written successfully.");
    }
}
