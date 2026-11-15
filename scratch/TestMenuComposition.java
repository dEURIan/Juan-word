import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestMenuComposition {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage title = ImageIO.read(new File("Title.png"));
        BufferedImage startBtn = ImageIO.read(new File("startbtn.png"));
        BufferedImage continueBtn = ImageIO.read(new File("continuebtn.png"));
        BufferedImage settingBtn = ImageIO.read(new File("settingbtn.png"));
        BufferedImage exitBtn = ImageIO.read(new File("exitbtn.png"));

        int w = 960;
        int h = 540;

        BufferedImage screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = screen.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Layer 1: Sky background
        g2.drawImage(bg, 0, 0, w, h, null);

        // 2. Layer 2: Mt Apo
        g2.drawImage(mtapo, 0, 0, w, h, null);

        // 3. Layer 3: Indigenous Frame
        g2.drawImage(frame, 0, 0, w, h, null);

        // 4. Layer 4: Title Logo (enlarged: 55% width)
        int titleW = (int) (w * 0.55);
        int titleH = titleW * 586 / 1163;
        int titleX = (w - titleW) / 2;
        int titleY = 16;
        g2.drawImage(title, titleX, titleY, titleW, titleH, null);

        // 5. Layer 5: Buttons (192x48)
        int btnW = 192;
        int btnH = 48;
        int btnX = (w - btnW) / 2;
        int gap = 6;
        int bottomMargin = 22;
        int totalBtnH = (btnH * 4) + (gap * 3);
        int startY = h - bottomMargin - totalBtnH;

        g2.drawImage(continueBtn, btnX, startY, btnW, btnH, null);
        g2.drawImage(startBtn, btnX, startY + (btnH + gap), btnW, btnH, null);
        g2.drawImage(settingBtn, btnX, startY + (btnH + gap) * 2, btnW, btnH, null);
        g2.drawImage(exitBtn, btnX, startY + (btnH + gap) * 3, btnW, btnH, null);

        g2.dispose();

        File out = new File("scratch/menu_composite_preview.png");
        ImageIO.write(screen, "png", out);
        System.out.println("Preview saved to " + out.getAbsolutePath());
    }
}
