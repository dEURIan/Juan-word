import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestCombineLayers {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage title = ImageIO.read(new File("Title.png"));

        int w = 960;
        int h = 540;

        // Combination A: bg -> mtapo -> indigframe
        BufferedImage comboA = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gA = comboA.createGraphics();
        gA.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gA.drawImage(bg, 0, 0, w, h, null);
        gA.drawImage(mtapo, 0, 0, w, h, null);
        gA.drawImage(frame, 0, 0, w, h, null);
        gA.dispose();
        ImageIO.write(comboA, "png", new File("scratch/combo_bg_mtapo_frame.png"));

        // Combination B: bg -> indigframe -> mtapo
        BufferedImage comboB = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gB = comboB.createGraphics();
        gB.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gB.drawImage(bg, 0, 0, w, h, null);
        gB.drawImage(frame, 0, 0, w, h, null);
        gB.drawImage(mtapo, 0, 0, w, h, null);
        gB.dispose();
        ImageIO.write(comboB, "png", new File("scratch/combo_bg_frame_mtapo.png"));

        System.out.println("Saved combinations A and B.");
    }
}
