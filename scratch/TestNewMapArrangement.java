import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestNewMapArrangement {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage blueBldg = ImageIO.read(new File("Blue Building.png")).getSubimage(0, 0, 648, 512);
        BufferedImage durianMuseum = ImageIO.read(new File("durianmuseum.png")).getSubimage(0, 300, 1080, 1620);
        BufferedImage sanPedro = ImageIO.read(new File("sanpedro.png")).getSubimage(0, 201, 1612, 865);

        int w = 960;
        int h = 540;

        // Create silhouettes for locked maps
        BufferedImage silMtApo = createSilhouette(mtapo, new Color(20, 25, 38, 245));
        BufferedImage silDurian = createSilhouette(durianMuseum, new Color(18, 22, 34, 250));
        BufferedImage silSanPedro = createSilhouette(sanPedro, new Color(18, 22, 34, 250));

        BufferedImage screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = screen.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sky background
        g2.drawImage(bg, 0, 0, w, h, null);

        // 2. Mt Apo at the back (Map 4 - Locked silhouette)
        g2.drawImage(silMtApo, 0, 0, w, h, null);

        // 3. Left: Blue Building (Map 1 - Unlocked full color)
        int bbH = (int)(h * 0.44);
        int bbW = (int)(bbH * (648.0 / 512.0));
        int bbX = 24;
        int bbY = h - bbH - 18;
        g2.drawImage(blueBldg, bbX, bbY, bbW, bbH, null);

        // 4. Center: Davao Museum (Map 2 - Locked silhouette)
        int dmW = (int)(w * 0.32);
        int dmH = (int)(h * 0.46);
        int dmX = (w - dmW) / 2 - 10;
        int dmY = h - dmH - 18;
        g2.drawImage(silDurian, dmX, dmY, dmW, dmH, null);

        // 5. Right: San Pedro (Map 3 - Locked silhouette)
        int spH = (int)(h * 0.48);
        int spW = (int)(spH * (1612.0 / 865.0));
        int spX = w - spW + 60;
        int spY = h - spH - 18;
        g2.drawImage(silSanPedro, spX, spY, spW, spH, null);

        // 6. Indigenous Frame on top
        g2.drawImage(frame, 0, 0, w, h, null);

        g2.dispose();

        File out = new File("scratch/test_new_map_arrangement.png");
        ImageIO.write(screen, "png", out);
        System.out.println("Saved new map arrangement to " + out.getAbsolutePath());
    }

    private static BufferedImage createSilhouette(BufferedImage src, Color color) {
        BufferedImage sil = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sil.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(color);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return sil;
    }
}
