import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FindExactMatch {
    public static void main(String[] args) throws Exception {
        BufferedImage mock = ImageIO.read(new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/.user_uploaded/media_1794751758936_3dc55716.jpg"));
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage sanpedro = ImageIO.read(new File("sanpedro.png"));
        BufferedImage clocktower = ImageIO.read(new File("clocktower.png"));
        BufferedImage durianmuseum = ImageIO.read(new File("durianmuseum.png"));

        // Match to 480x270 (mockup aspect)
        int mw = mock.getWidth(); // 480
        int mh = mock.getHeight(); // 273

        // Crop tight landmarks
        BufferedImage ctTight = clocktower.getSubimage(269, 96, 616, 1744);
        BufferedImage dmTight = durianmuseum.getSubimage(0, 300, 1080, 1620);

        // San Pedro in 1920x1080: bounds [0, 201 to 1611, 1065]
        BufferedImage spTight = sanpedro.getSubimage(0, 201, 1612, 865);

        // Let us generate a preview at 960x540
        int w = 960;
        int h = 540;

        BufferedImage comp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = comp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sky & Mt Apo
        g2.drawImage(bg, 0, 0, w, h, null);
        g2.drawImage(mtapo, 0, 0, w, h, null);

        // In the mockup:
        // 2. San Pedro: covers left side.
        // In 960x540, if drawn with bottom at h - 16, height ~ 260px:
        int spH = (int)(h * 0.52); // ~280px
        int spW = (int)(spH * (1612.0 / 865.0)); // ~521px
        int spX = 16; // 16px from left frame
        int spY = h - spH - 16;
        g2.drawImage(spTight, spX, spY, spW, spH, null);

        // 3. Durian Museum: covers right side.
        // In mockup: dome facets span from x ~ 620 to 940 (width ~ 320px).
        // Height ~ 260px.
        int dmH = (int)(h * 0.50); // ~270px
        int dmW = (int)(dmH * (1080.0 / 1620.0) * 1.5); // wait, let's check durian aspect ratio
        // In raw durianmuseum.png: width is 1080, height is 1620. Ratio is 1080/1620 = 0.667.
        // But in mockup, durian dome is wider! Let's check durian width in mockup!
        int dmW_mock = (int)(w * 0.36); // ~345px
        int dmH_mock = (int)(h * 0.49); // ~265px
        int dmX = w - dmW_mock - 16;
        int dmY = h - dmH_mock - 16;
        g2.drawImage(dmTight, dmX, dmY, dmW_mock, dmH_mock, null);

        // 4. Clock Tower: in center.
        // In mockup: clock tower is centered at x = 480.
        // Clock tower top reaches y = 140 (about 26% of h).
        // Height is about h - 140 - 16 = 384px (71% of h).
        int ctH = (int)(h * 0.74); // ~400px
        int ctW = (int)(ctH * (616.0 / 1744.0)); // ~141px
        int ctX = (w - ctW) / 2; // ~409 to 550
        int ctY = h - ctH - 16;
        g2.drawImage(ctTight, ctX, ctY, ctW, ctH, null);

        // 5. Indigenous Frame on top
        g2.drawImage(frame, 0, 0, w, h, null);

        g2.dispose();

        File out = new File("scratch/perfect_map_mockup_match.png");
        ImageIO.write(comp, "png", out);
        System.out.println("Saved comparison to " + out.getAbsolutePath());
    }
}
