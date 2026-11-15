import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PrototypeTileAndBgFix {
    public static void main(String[] args) throws Exception {
        BufferedImage sky = ImageIO.read(new File("src/juanword/bg.png"));
        BufferedImage frame = ImageIO.read(new File("src/juanword/indigframe.png"));
        BufferedImage blueBldg = ImageIO.read(new File("src/juanword/bluebuilding.png"));
        BufferedImage museum = ImageIO.read(new File("src/juanword/durianmuseum.png"));
        BufferedImage sanPedro = ImageIO.read(new File("src/juanword/sanpedro.png"));
        BufferedImage mtApo = ImageIO.read(new File("src/juanword/mtapo.png"));

        int w = 960;
        int h = 540;

        BufferedImage[] icons = {blueBldg, museum, sanPedro, mtApo};
        String[] names = {"Blue Building", "Davao Museum", "San Pedro Cathedral", "Mt Apo"};

        for (int i = 0; i < icons.length; i++) {
            BufferedImage comp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = comp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 1. Sky & Cloud Background
            g.drawImage(sky, 0, 0, w, h, null);

            // 2. Landmark Icon
            BufferedImage icon = icons[i];
            if (icon != null) {
                int iconW = icon.getWidth();
                int iconH = icon.getHeight();
                // Scale landmark to sit prominently behind combat arena
                double scale = Math.min((double) (w - 120) / iconW, (double) (h - 80) / iconH);
                int dw = (int) Math.round(iconW * scale);
                int dh = (int) Math.round(iconH * scale);
                int dx = (w - dw) / 2;
                int dy = h - dh - 30; // Anchored near bottom inside frame
                g.drawImage(icon, dx, dy, dw, dh, null);
            }

            // 3. Indigenous Border Frame
            g.drawImage(frame, 0, 0, w, h, null);

            // 4. Soft Scrim for readability
            g.setColor(new Color(12, 16, 52, 90));
            g.fillRect(0, 0, w, h);

            g.dispose();

            File out = new File("scratch/test_battle_bg_" + i + ".png");
            ImageIO.write(comp, "PNG", out);
            System.out.println("Rendered background for " + names[i] + " to " + out.getAbsolutePath());
        }
    }
}
